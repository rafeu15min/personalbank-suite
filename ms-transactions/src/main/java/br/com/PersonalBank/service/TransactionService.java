package br.com.PersonalBank.service;

import br.com.PersonalBank.client.BankApiClient;
import br.com.PersonalBank.common.client.ApiClientProvider;
import br.com.PersonalBank.dto.Transaction;
import br.com.PersonalBank.event.TransactionRequestEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TransactionService {

    private static final Logger LOGGER = Logger.getLogger(TransactionService.class);

    @Inject
    ApiClientProvider apiClientProvider;

    public Uni<Void> fetchAndProcessTransactions(TransactionRequestEvent event) {
        String institutionKey = event.institutionKey();
        String accountId = event.accountId();

        // 1. Obtém o cliente dinamicamente
        BankApiClient bankClient = apiClientProvider.buildClient(
                BankApiClient.class,
                institutionKey,
                "accounts");

        String accessToken = "Bearer " + event.userToken();

        // 2. Chama o cliente de forma reativa e processa o resultado
        return bankClient.getTransactions(accountId, accessToken)
                .onItem().invoke(response -> {
                    // Lógica de sucesso
                    LOGGER.infof("Sucesso! %d transações encontradas para a conta [%s] no banco [%s]",
                            response.data.size(), accountId, institutionKey);

                    // Aqui entraria a lógica para salvar no banco de dados
                    // ex: transactionRepository.persist(response.data);
                })
                .onFailure().invoke(failure -> {
                    // Lógica de falha
                    LOGGER.errorf(failure, "ERRO ao processar transações para a conta [%s] no banco [%s]",
                            accountId, institutionKey);

                    // Aqui entraria a lógica de tratamento de erro (ex: enviar para dead letter
                    // queue)
                })
                .replaceWithVoid(); // Transforma o resultado final em Uni<Void>
    }
}