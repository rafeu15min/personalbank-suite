package br.com.PersonalBank.client;

import br.com.PersonalBank.dto.auth.CreateConsentRequest;
import br.com.PersonalBank.dto.auth.CreateConsentResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/** Cliente da API de consentimentos de um banco (família "consents" no banks.yml). */
@RegisterRestClient
public interface ConsentApiClient {

    @POST
    @Path("/consents")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CreateConsentResponse createConsent(
            CreateConsentRequest body,
            @HeaderParam("Authorization") String authorizationHeader);
}
