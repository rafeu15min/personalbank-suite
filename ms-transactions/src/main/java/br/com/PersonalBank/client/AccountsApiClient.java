package br.com.PersonalBank.client;

import br.com.PersonalBank.common.dto.OpenFinanceResponse;
import br.com.PersonalBank.dto.account.Account;
import br.com.PersonalBank.dto.transaction.Transaction;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente da API de contas (família "accounts" no banks.yml, que já inclui a versão).
 */
@RegisterRestClient
@Produces(MediaType.APPLICATION_JSON)
public interface AccountsApiClient {

    @GET
    @Path("/accounts")
    OpenFinanceResponse<List<Account>> listAccounts(@HeaderParam("Authorization") String authorizationHeader);

    @GET
    @Path("/accounts/{accountId}/transactions")
    OpenFinanceResponse<List<Transaction>> getTransactions(
            @PathParam("accountId") String accountId,
            @HeaderParam("Authorization") String authorizationHeader);
}
