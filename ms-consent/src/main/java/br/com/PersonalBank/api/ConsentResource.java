package br.com.PersonalBank.api;

import br.com.PersonalBank.dto.InitiateConsentRequest;
import br.com.PersonalBank.dto.InitiateConsentResponse;
import br.com.PersonalBank.service.ConsentService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@Path("/api/consents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConsentResource {

    @Inject
    ConsentService consentService; // Injeta a camada de serviço que orquestra a lógica

    /**
     * Endpoint para iniciar o fluxo de consentimento do Open Finance.
     * O frontend chama este endpoint quando o usuário escolhe um banco.
     *
     * @param request Contém a 'institutionKey' do banco selecionado.
     * @return Uma resposta contendo a URL de redirecionamento do banco.
     */
    @POST
    @Path("/initiate")
    public Uni<InitiateConsentResponse> initiateConsent(InitiateConsentRequest request) {
        // Delega a lógica complexa para a camada de serviço
        return consentService.initiateConsentFlow(request.institutionKey());
    }

    /**
     * Endpoint de callback para onde o banco redireciona o usuário após a autorização.
     *
     * @param code O código de autorização fornecido pelo banco.
     * @param consentId O ID do consentimento relacionado.
     * @return Um redirecionamento para uma página de sucesso ou erro no frontend.
     */
    @GET
    @Path("/callback")
    public Uni<Response> handleCallback(@QueryParam("code") String code, @QueryParam("consentId") String consentId) {
        
        // Delega a troca do código pelo token para a camada de serviço
        return consentService.exchangeCodeForTokens(code, consentId)
                .onItem().transform(success -> {
                    // Se o fluxo foi bem-sucedido, redireciona para a página de sucesso do frontend
                    URI successUri = URI.create("https://app.personalbank.com/dashboard?status=success");
                    return Response.seeOther(successUri).build();
                })
                .onFailure().recoverWithItem(failure -> {
                    // Se falhou, redireciona para a página de erro do frontend
                    URI errorUri = URI.create("https://app.personalbank.com/dashboard?status=error&message=" + failure.getMessage());
                    return Response.seeOther(errorUri).build();
                });
    }
}