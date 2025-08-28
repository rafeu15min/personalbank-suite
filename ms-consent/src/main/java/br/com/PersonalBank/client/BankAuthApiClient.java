package br.com.PersonalBank.client;

import br.com.PersonalBank.dto.auth.CreateConsentRequest;
import br.com.PersonalBank.dto.auth.CreateConsentResponse;
import br.com.PersonalBank.dto.auth.TokenResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente REST para se comunicar com os Endpoints de Autorização
 * e Consentimento dos bancos.
 */
@RegisterRestClient
public interface BankAuthApiClient {

    /**
     * Inicia o fluxo de consentimento com a instituição financeira.
     */
    @POST
    @Path("/consents") // O caminho pode variar ligeiramente entre bancos
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<CreateConsentResponse> createConsent(
            CreateConsentRequest body,
            @HeaderParam("Authorization") String authorizationHeader
    );

    /**
     * Troca o código de autorização (authorization_code) por um access_token.
     * Esta chamada usa o formato 'form-urlencoded', como exigido pelo padrão OAuth 2.0.
     */
    @POST
    @Path("/token") // O caminho do endpoint de token
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<TokenResponse> exchangeCodeForToken(
            @FormParam("grant_type") String grantType, // Deve ser "authorization_code"
            @FormParam("code") String code,
            @FormParam("redirect_uri") String redirectUri,
            @FormParam("client_id") String clientId
            // A autenticação do cliente (client_secret ou private_key_jwt) geralmente
            // é enviada no Header de Autorização, dependendo do banco.
    );
}