package br.com.PersonalBank.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO que mapeia a resposta bem-sucedida do endpoint de token do OAuth 2.0.
 */
public record TokenResponse(
    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("refresh_token")
    String refreshToken,

    @JsonProperty("expires_in")
    int expiresIn, // Tempo de vida do access_token em segundos

    @JsonProperty("token_type")
    String tokenType, // Geralmente "Bearer"

    @JsonProperty("scope")
    String scope
) {}