package br.com.PersonalBank.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO que mapeia a resposta da criação de consentimento.
 * O campo mais importante é a URL de redirecionamento.
 */
public record CreateConsentResponse(
    @JsonProperty("data")
    Data data,

    @JsonProperty("links")
    Links links
) {
    public record Data(
        @JsonProperty("consentId")
        String consentId,

        @JsonProperty("status")
        String status, // Ex: "AWAITING_AUTHORISATION"

        @JsonProperty("creationDateTime")
        String creationDateTime
    ) {}

    public record Links(
        @JsonProperty("self")
        String self
    ) {}
}