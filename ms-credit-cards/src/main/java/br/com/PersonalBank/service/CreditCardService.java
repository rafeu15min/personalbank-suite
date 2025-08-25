package br.com.PersonalBank.service;

import br.com.PersonalBank.client.CreditCardApiClient;
import br.com.PersonalBank.common.client.ApiClientProvider;
import br.com.PersonalBank.common.event.InitialLoadEvent;
import br.com.PersonalBank.event.UpdateCardEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Optional;

@ApplicationScoped
public class CreditCardService {

    private static final Logger LOGGER = Logger.getLogger(CreditCardService.class);

    @Inject
    ApiClientProvider apiClientProvider;

    public Uni<Void> performInitialLoad(InitialLoadEvent event) {
        String institutionKey = event.institutionKey();
        String accountId = event.accountId();
        String accessToken = "Bearer " + event.userToken();

        Optional<CreditCardApiClient> clientOptional = apiClientProvider.buildClient(
                CreditCardApiClient.class,
                institutionKey,
                "credit-cards");

        if (clientOptional.isEmpty()) {
            LOGGER.warnf(
                    "API de Cartões de Crédito não configurada para a instituição [%s]. A carga inicial será ignorada.",
                    institutionKey);
            return Uni.createFrom().voidItem();
        }

        CreditCardApiClient client = clientOptional.get();
        LOGGER.infof("Iniciando carga inicial de cartões para conta [%s] no banco [%s]", accountId, institutionKey);

        return client.getAccount(accountId, accessToken)
                .onItem().invoke(accountResponse -> LOGGER.infof("Dados da conta do cartão [%s] obtidos.", accountId))
                .onItem().transformToUni(accountResponse -> client.getInvoices(accountId, accessToken))
                .onItem().invoke(invoicesResponse -> {
                    LOGGER.infof("%d faturas encontradas para a conta [%s]", invoicesResponse.data.size(), accountId);
                    // Lógica de paginação e busca das transações de cada fatura...
                })
                .onFailure()
                .invoke(failure -> LOGGER.errorf(failure, "Falha na carga inicial para conta [%s]", accountId))
                .replaceWithVoid();
    }

    public Uni<Void> performUpdate(UpdateCardEvent event) {
        String institutionKey = event.institutionKey();
        String accountId = event.accountId();

        Optional<CreditCardApiClient> clientOptional = apiClientProvider.buildClient(
                CreditCardApiClient.class,
                institutionKey,
                "credit-cards");

        if (clientOptional.isEmpty()) {
            LOGGER.warnf(
                    "API de Cartões de Crédito não configurada para a instituição [%s]. A atualização será ignorada.",
                    institutionKey);
            return Uni.createFrom().voidItem();
        }

        LOGGER.infof("Iniciando atualização de cartões para conta [%s] no banco [%s]", accountId, institutionKey);

        // Lógica de atualização (buscar faturas recentes, etc.)...

        return Uni.createFrom().voidItem();
    }
}