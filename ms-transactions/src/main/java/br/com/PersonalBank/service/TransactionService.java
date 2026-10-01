package br.com.PersonalBank.service;

import br.com.PersonalBank.client.AccountsApiClient;
import br.com.PersonalBank.common.client.ApiClientProvider;
import br.com.PersonalBank.common.client.ConsentTokenClient;
import br.com.PersonalBank.common.event.InitialLoadEvent;
import br.com.PersonalBank.dto.account.Account;
import br.com.PersonalBank.dto.transaction.Transaction;
import br.com.PersonalBank.entity.AccountTransaction;
import br.com.PersonalBank.entity.BankAccount;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TransactionService {

    private static final Logger LOGGER = Logger.getLogger(TransactionService.class);

    @Inject
    ApiClientProvider apiClientProvider;

    @Inject
    @RestClient
    ConsentTokenClient consentTokenClient;

    /**
     * Busca as contas e transações do consentimento no banco e grava tudo.
     * As chamadas HTTP acontecem antes, fora da transação do banco de dados.
     */
    public void performInitialLoad(InitialLoadEvent event) {
        String institutionKey = event.institutionKey();
        String consentId = event.consentId();

        Optional<AccountsApiClient> clientOptional = apiClientProvider.buildClient(
                AccountsApiClient.class,
                institutionKey,
                "accounts");
        if (clientOptional.isEmpty()) {
            LOGGER.warnf("API de Contas não configurada para a instituição [%s]. A carga inicial será ignorada.",
                    institutionKey);
            return;
        }

        AccountsApiClient client = clientOptional.get();
        String authorization = "Bearer " + consentTokenClient.getAccessToken(consentId);
        LOGGER.infof("Iniciando carga inicial de transações do consentimento [%s] no banco [%s]", consentId,
                institutionKey);

        Map<Account, List<Transaction>> transactionsByAccount = new LinkedHashMap<>();
        for (Account account : client.listAccounts(authorization).data) {
            transactionsByAccount.put(account, client.getTransactions(account.accountId(), authorization).data);
        }

        QuarkusTransaction.requiringNew().run(() -> transactionsByAccount.forEach((account, transactions) -> {
            BankAccount.upsert(consentId, account);
            transactions.forEach(transaction -> AccountTransaction.upsert(consentId, account.accountId(), transaction));
        }));

        LOGGER.infof("Carga inicial do consentimento [%s] concluída: %d contas, %d transações.", consentId,
                transactionsByAccount.size(),
                transactionsByAccount.values().stream().mapToInt(List::size).sum());
    }
}
