package br.com.PersonalBank.client;

import br.com.PersonalBank.common.dto.OpenFinanceResponse;
import br.com.PersonalBank.dto.card.CreditCardAccount;
import br.com.PersonalBank.dto.card.CreditCardInvoice;
import br.com.PersonalBank.dto.card.CreditCardTransaction;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente da API de cartões de crédito (família "credit-cards" no banks.yml,
 * que já inclui a versão).
 */
@RegisterRestClient
@Produces(MediaType.APPLICATION_JSON)
public interface CreditCardApiClient {

    @GET
    @Path("/accounts")
    OpenFinanceResponse<List<CreditCardAccount>> listAccounts(
            @HeaderParam("Authorization") String authorizationHeader);

    @GET
    @Path("/accounts/{accountId}/bills")
    OpenFinanceResponse<List<CreditCardInvoice>> getBills(
            @PathParam("accountId") String accountId,
            @HeaderParam("Authorization") String authorizationHeader);

    @GET
    @Path("/accounts/{accountId}/transactions")
    OpenFinanceResponse<List<CreditCardTransaction>> getTransactions(
            @PathParam("accountId") String accountId,
            @HeaderParam("Authorization") String authorizationHeader);
}
