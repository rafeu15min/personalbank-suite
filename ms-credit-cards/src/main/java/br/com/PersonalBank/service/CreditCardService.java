package br.com.PersonalBank.service;

import br.com.PersonalBank.client.CreditCardApiClient;
import br.com.PersonalBank.common.client.ApiClientProvider;
import br.com.PersonalBank.common.event.InitialLoadEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreditCardService {

    private static final Logger LOGGER = Logger.getLogger(CreditCardService.class);

    @Inject
    ApiClientProvider apiClientProvider;

    public Uni<Void> performInitialLoad(InitialLoadEvent event) {
        String institutionKey = event.getInstitutionKey();
        String accountId = event.getAccountId();
        String accessToken = "Bearer " + event.getUserToken();

        LOGGER.infof("Iniciando carga inicial de cartões para conta [%s] no banco [%s]", accountId, institutionKey);

        CreditCardApiClient client = apiClientProvider.buildClient(
                CreditCardApiClient.class,
                institutionKey,
                "credit-cards");

        // Lógica complexa de carga inicial:
        // 1. Pega os dados da conta do cartão.
        // 2. Pega a lista de faturas (precisaria de lógica de paginação aqui em um caso
        // real).
        // 3. Para cada fatura, pega a lista de transações (também paginada).
        return client.getAccount(accountId, accessToken)
                .onItem().invoke(accountResponse -> {
                    LOGGER.infof("Dados da conta do cartão [%s] obtidos.", accountId);
                    // Salvar dados da conta no BD...
                })
                .onItem().transformToUni(accountResponse -> client.getInvoices(accountId, accessToken))
                .onItem().invoke(invoicesResponse -> {
                    LOGGER.infof("%d faturas encontradas para a conta [%s]", invoicesResponse.data.size(), accountId);
                    // Para cada fatura, buscar as transações (simplificado, sem paginação)
                    for (var invoice : invoicesResponse.data) {
                        client.getInvoiceTransactions(accountId, invoice.invoiceId(), accessToken)
                                .onItem().invoke(transactionsResponse -> {
                                    LOGGER.infof("  -> %d transações encontradas para a fatura [%s]",
                                            transactionsResponse.data.size(), invoice.invoiceId());
                                    // Salvar faturas e transações no BD...
                                }).subscribe().with(item -> {
                                }, failure -> LOGGER.errorf(failure, "Falha ao buscar transações da fatura %s",
                                        invoice.invoiceId()));
                    }
                })
                .onFailure()
                .invoke(failure -> LOGGER.errorf(failure, "Falha na carga inicial para conta [%s]", accountId))
                .replaceWithVoid();
    }

    // public Uni<Void> performUpdate(UpdateCardEvent event) { ... }
}