package br.com.PersonalBank.mockbank;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/** API de consentimentos simulada. Exige um token client_credentials. */
@Path("/open-banking/consents/v3/consents")
public class ConsentResource {

    @Inject
    MockBankState state;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@HeaderParam("Authorization") String authorization) {
        state.requireClientToken(authorization);
        String consentId = state.createConsent();
        var body = Map.of(
                "data", Map.of(
                        "consentId", consentId,
                        "status", MockBankState.AWAITING_AUTHORISATION,
                        "creationDateTime", OffsetDateTime.now(ZoneOffset.UTC).toString()),
                "links", Map.of("self", "/open-banking/consents/v3/consents/" + consentId));
        return Response.status(Response.Status.CREATED).entity(body).build();
    }
}
