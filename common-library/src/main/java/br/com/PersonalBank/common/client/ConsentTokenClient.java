package br.com.PersonalBank.common.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente da API interna do ms-consent, usado pelos serviços de dados para
 * obter o token de acesso de um consentimento já autorizado.
 * A URL vem de quarkus.rest-client.ms-consent.url.
 */
@RegisterRestClient(configKey = "ms-consent")
@Path("/internal/consents")
public interface ConsentTokenClient {

    @GET
    @Path("/{consentId}/access-token")
    @Produces(MediaType.TEXT_PLAIN)
    String getAccessToken(@PathParam("consentId") String consentId);
}
