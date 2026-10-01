package br.com.PersonalBank.api;

import br.com.PersonalBank.dto.ConsentStatusResponse;
import br.com.PersonalBank.dto.InitiateConsentRequest;
import br.com.PersonalBank.dto.InitiateConsentResponse;
import br.com.PersonalBank.entity.UserConsent;
import br.com.PersonalBank.service.ConsentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/consents")
@Produces(MediaType.APPLICATION_JSON)
public class ConsentResource {

    @Inject
    ConsentService consentService;

    /**
     * Inicia o fluxo de consentimento. O frontend chama este endpoint quando o
     * usuário escolhe um banco e depois redireciona o usuário para redirectUrl.
     */
    @POST
    @Path("/initiate")
    @Consumes(MediaType.APPLICATION_JSON)
    public InitiateConsentResponse initiateConsent(InitiateConsentRequest request) {
        if (request == null || request.institutionKey() == null || request.institutionKey().isBlank()) {
            throw ConsentService.badRequest("institutionKey é obrigatório");
        }
        return consentService.initiateConsentFlow(request.institutionKey());
    }

    /**
     * Callback para onde o banco redireciona o usuário após a autorização.
     * Enquanto não há frontend, responde com o status do consentimento em JSON.
     */
    @GET
    @Path("/callback")
    public ConsentStatusResponse handleCallback(@QueryParam("code") String code, @QueryParam("state") String state) {
        if (code == null || code.isBlank()) {
            throw ConsentService.badRequest("Parâmetro code ausente");
        }
        UserConsent consent = consentService.completeAuthorisation(code, state);
        return new ConsentStatusResponse(consent.consentId, consent.institutionKey, consent.status);
    }
}
