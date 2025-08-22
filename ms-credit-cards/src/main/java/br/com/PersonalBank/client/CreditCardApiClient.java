package br.com.PersonalBank.client;

import br.com.PersonalBank.common.dto.OpenFinanceResponse;
import br.com.PersonalBank.dto.card.CreditCardAccount;
import br.com.PersonalBank.dto.card.CreditCardInvoice;
import br.com.PersonalBank.dto.card.CreditCardTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient
@Path("/credit-cards-accounts/v1")
public interface CreditCardApiClient {

    @GET
    @Path("/{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<OpenFinanceResponse<CreditCardAccount>> getAccount(
            @PathParam("accountId") String accountId,
            @HeaderParam("Authorization") String authorizationHeader);

    @GET
    @Path("/{accountId}/invoices")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<OpenFinanceResponse<List<CreditCardInvoice>>> getInvoices(
            @PathParam("accountId") String accountId,
            @HeaderParam("Authorization") String authorizationHeader);

    @GET
    @Path("/{accountId}/invoices/{invoiceId}/transactions")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<OpenFinanceResponse<List<CreditCardTransaction>>> getInvoiceTransactions(
            @PathParam("accountId") String accountId,
            @PathParam("invoiceId") String invoiceId,
            @HeaderParam("Authorization") String authorizationHeader);
}