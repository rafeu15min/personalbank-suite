package br.com.PersonalBank.client;

import br.com.PersonalBank.common.dto.OpenFinanceResponse;
import br.com.PersonalBank.dto.investment.InvestmentPosition;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Interface do Cliente REST para interagir com a API de Investimentos
 * do Open Finance, seguindo o padrão reativo com Mutiny.
 *
 * Esta interface será instanciada dinamicamente pela nossa ApiClientProvider.
 */
@RegisterRestClient
@Path("/investments/v1") // Path base para a API de Investimentos v1
public interface InvestmentsApiClient {

    /**
     * Busca a posição consolidada de investimentos para uma conta específica.
     * O Open Finance geralmente usa um ID de consentimento ou consolidação.
     *
     * @param consentId           O ID do consentimento ou da conta consolidada de
     *                            investimentos.
     * @param authorizationHeader O header de autorização com o token de acesso (ex:
     *                            "Bearer <token>").
     * @return Um objeto reativo (Uni) que, quando resolvido, conterá a resposta
     *         com a posição consolidada de investimentos (InvestmentPosition).
     */
    @GET
    @Path("/consolidated/{consentId}") // Exemplo de endpoint para posição consolidada
    @Produces(MediaType.APPLICATION_JSON)
    Uni<OpenFinanceResponse<InvestmentPosition>> getConsolidatedPosition(
            @PathParam("consentId") String consentId,
            @HeaderParam("Authorization") String authorizationHeader);

    /*
     * Futuramente, outros métodos poderiam ser adicionados para buscar, por
     * exemplo,
     * o extrato de transações de um produto de investimento específico.
     * 
     * @GET
     * 
     * @Path("/fixed-income/{investmentId}/transactions")
     * Uni<OpenFinanceResponse<List<ProductTransaction>>>
     * getFixedIncomeTransactions(
     * 
     * @PathParam("investmentId") String investmentId,
     * 
     * @HeaderParam("Authorization") String authorizationHeader
     * );
     */
}