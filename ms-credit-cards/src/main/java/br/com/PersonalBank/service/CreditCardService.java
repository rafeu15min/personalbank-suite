package br.com.PersonalBank.service;

import br.com.PersonalBank.client.CreditCardApiClient;
import br.com.PersonalBank.common.client.ApiClientProvider;
import br.com.PersonalBank.common.client.ConsentTokenClient;
import br.com.PersonalBank.common.event.InitialLoadEvent;
import br.com.PersonalBank.dto.card.CreditCardAccount;
import br.com.PersonalBank.dto.card.CreditCardInvoice;
import br.com.PersonalBank.dto.card.CreditCardTransaction;
import br.com.PersonalBank.entity.CardAccount;
import br.com.PersonalBank.entity.CardBill;
import br.com.PersonalBank.entity.CardTransaction;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreditCardService {

    private static final Logger LOGGER = Logger.getLogger(CreditCardService.class);

    @Inject
    ApiClientProvider apiClientProvider;

    @Inject
    @RestClient
    ConsentTokenClient consentTokenClient;

    /** Tudo o que foi buscado de um cartão, antes de gravar. */
    private record CardData(
            CreditCardAccount account,
            List<CreditCardInvoice> bills,
            List<CreditCardTransaction> transactions) {
    }

    /**
     * Busca cartões, faturas e lançamentos do consentimento no banco e grava tudo.
     * As chamadas HTTP acontecem antes, fora da transação do banco de dados.
     */
    public void performInitialLoad(InitialLoadEvent event) {
        String institutionKey = event.institutionKey();
        String consentId = event.consentId();

        Optional<CreditCardApiClient> clientOptional = apiClientProvider.buildClient(
                CreditCardApiClient.class,
                institutionKey,
                "credit-cards");
        if (clientOptional.isEmpty()) {
            LOGGER.warnf(
                    "API de Cartões de Crédito não configurada para a instituição [%s]. A carga inicial será ignorada.",
                    institutionKey);
            return;
        }

        CreditCardApiClient client = clientOptional.get();
        String authorization = "Bearer " + consentTokenClient.getAccessToken(consentId);
        LOGGER.infof("Iniciando carga inicial de cartões do consentimento [%s] no banco [%s]", consentId,
                institutionKey);

        List<CardData> cards = new ArrayList<>();
        for (CreditCardAccount account : client.listAccounts(authorization).data) {
            String accountId = account.creditCardAccountId();
            cards.add(new CardData(
                    account,
                    client.getBills(accountId, authorization).data,
                    client.getTransactions(accountId, authorization).data));
        }

        QuarkusTransaction.requiringNew().run(() -> cards.forEach(card -> {
            String accountId = card.account().creditCardAccountId();
            CardAccount.upsert(consentId, card.account());
            card.bills().forEach(bill -> CardBill.upsert(consentId, accountId, bill));
            card.transactions().forEach(transaction -> CardTransaction.upsert(consentId, accountId, transaction));
        }));

        LOGGER.infof("Carga inicial de cartões do consentimento [%s] concluída: %d cartões, %d faturas, %d lançamentos.",
                consentId, cards.size(),
                cards.stream().mapToInt(card -> card.bills().size()).sum(),
                cards.stream().mapToInt(card -> card.transactions().size()).sum());
    }
}
