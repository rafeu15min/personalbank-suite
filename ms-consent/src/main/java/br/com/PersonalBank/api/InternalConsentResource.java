package br.com.PersonalBank.api;

import br.com.PersonalBank.service.ConsentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * API interna, usada pelos serviços de dados para obter o token de um
 * consentimento. Ainda não tem autenticação entre serviços: no docker compose
 * só a rede interna deveria alcançá-la.
 */
@Path("/internal/consents")
public class InternalConsentResource {

    @Inject
    ConsentService consentService;

    @GET
    @Path("/{consentId}/access-token")
    @Produces(MediaType.TEXT_PLAIN)
    public String getAccessToken(@PathParam("consentId") String consentId) {
        return consentService.getAccessToken(consentId);
    }
}
