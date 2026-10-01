package br.com.PersonalBank.client;

import br.com.PersonalBank.dto.auth.TokenResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente do endpoint de token OAuth 2.0 de um banco (família "auth" no banks.yml).
 * As chamadas usam form-urlencoded, como exige o OAuth 2.0.
 */
@RegisterRestClient
public interface BankAuthApiClient {

    /** Token da própria aplicação, usado para criar consentimentos. */
    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    TokenResponse requestToken(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId);

    /** Troca o código recebido no callback pelos tokens do usuário. */
    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    TokenResponse exchangeCode(
            @FormParam("grant_type") String grantType,
            @FormParam("code") String code,
            @FormParam("redirect_uri") String redirectUri,
            @FormParam("client_id") String clientId);
}
