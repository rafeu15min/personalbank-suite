package br.com.PersonalBank.service;

import br.com.PersonalBank.client.BankApiClient;
import br.com.PersonalBank.common.client.ApiClientProvider;
import br.com.PersonalBank.common.event.InitialLoadEvent; // Usando o evento genérico
import br.com.PersonalBank.dto.transaction.Transaction;
import br.com.PersonalBank.common.dto.OpenFinanceResponse;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TransactionService {

    private static final Logger LOGGER = Logger.getLogger(TransactionService.class);

    @Inject
    ApiClientProvider apiClientProvider;

    /**
     * Processa a solicitação de busca de transações para uma conta.
     * Agora verifica se a API está configurada antes de prosseguir.
     *
     * @param event O evento contendo os detalhes da solicitação.
     * @return Um Uni<Void> que representa a conclusão da operação assíncrona.
     */
    public Uni<Void> fetchAndProcessTransactions(InitialLoadEvent event) { // Alterado para usar o evento genérico
        String institutionKey = event.institutionKey();
        String accountId = event.accountId();
        String accessToken = "Bearer " + event.userToken();

        LOGGER.infof("Processando solicitação de transações para a instituição [%s] e conta [%s]", institutionKey,
                accountId);

        // 1. Pede o cliente e recebe um Optional
        Optional<BankApiClient> clientOptional = apiClientProvider.buildClient(
                BankApiClient.class,
                institutionKey,
                "accounts");

        // 2. Verifica se o cliente foi criado com sucesso
        if (clientOptional.isEmpty()) {
            LOGGER.warnf("API de Contas não configurada para a instituição [%s]. A solicitação será ignorada.",
                    institutionKey);
            return Uni.createFrom().voidItem(); // Finaliza com sucesso
        }

        // 3. Se o cliente existe, o fluxo normal continua
        BankApiClient client = clientOptional.get();

        return client.getTransactions(accountId, accessToken)
                .onItem().invoke(response -> {
                    LOGGER.infof("Sucesso! %d transações encontradas para a conta [%s] no banco [%s]",
                            response.data.size(), accountId, institutionKey);
                    // Lógica para salvar no banco de dados...
                })
                .onFailure().invoke(failure -> {
                    LOGGER.errorf(failure, "Falha ao buscar transações para a conta [%s] no banco [%s]",
                            accountId, institutionKey);
                })
                .replaceWithVoid();
    }
}