package br.com.PersonalBank.client;

import br.com.PersonalBank.common.dto.OpenFinanceResponse;
import br.com.PersonalBank.dto.transaction.Transaction;
import io.smallrye.mutiny.Uni; // Importação do Mutiny
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.List;

@RegisterRestClient
@Path("/accounts/v1")
public interface BankApiClient {

    @GET
    @Path("/{accountId}/transactions")
    @Produces(MediaType.APPLICATION_JSON)
    // ** MUDANÇA PRINCIPAL **
    // O método agora retorna um Uni, tornando a chamada não-bloqueante.
    Uni<OpenFinanceResponse<List<Transaction>>> getTransactions(
            @PathParam("accountId") String accountId,
            @HeaderParam("Authorization") String authorizationHeader);
}