package br.com.PersonalBank.mockbank;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.util.Map;

/**
 * Servidor de autorização simulado. Segue o formato do fluxo OAuth 2.0 do
 * Open Finance (client_credentials -> consentimento -> authorization_code),
 * sem mTLS, PAR nem assinatura JWT.
 */
@Path("/")
public class AuthResource {

    private static final int EXPIRES_IN_SECONDS = 900;

    @Inject
    MockBankState state;

    @POST
    @Path("token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> token(@FormParam("grant_type") String grantType, @FormParam("code") String code) {
        String accessToken = switch (grantType == null ? "" : grantType) {
            case "client_credentials" -> state.issueClientToken();
            case "authorization_code" -> state.exchangeCode(code)
                    .orElseThrow(() -> oauthError("invalid_grant"));
            default -> throw oauthError("unsupported_grant_type");
        };
        return Map.of(
                "access_token", accessToken,
                "refresh_token", "rt-" + accessToken,
                "expires_in", EXPIRES_IN_SECONDS,
                "token_type", "Bearer",
                "scope", "openid consents accounts credit-cards-accounts resources");
    }

    /**
     * Página de autorização. Num banco real o usuário faria login e confirmaria
     * o compartilhamento; aqui o consentimento é aprovado na hora e o navegador
     * volta para o redirect_uri com o código.
     */
    @GET
    @Path("authorize")
    public Response authorize(
            @QueryParam("consent_id") String consentId,
            @QueryParam("redirect_uri") String redirectUri,
            @QueryParam("state") String oauthState) {
        if (redirectUri == null) {
            throw oauthError("invalid_request");
        }
        String code = state.authorise(consentId).orElseThrow(() -> oauthError("invalid_request"));
        return Response.seeOther(UriBuilder.fromUri(redirectUri)
                .queryParam("code", code)
                .queryParam("state", oauthState)
                .build()).build();
    }

    private static BadRequestException oauthError(String error) {
        return new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", error))
                .build());
    }
}
