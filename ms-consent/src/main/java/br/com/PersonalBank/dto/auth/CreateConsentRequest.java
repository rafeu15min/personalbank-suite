package br.com.PersonalBank.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO para o corpo da requisição de criação de consentimento (POST /consents).
 * Define os dados que queremos acessar e por quanto tempo.
 */
public record CreateConsentRequest(
    @JsonProperty("data")
    Data data
) {
    public record Data(
        @JsonProperty("permissions")
        List<String> permissions, // Ex: ["ACCOUNTS_READ", "TRANSACTIONS_READ"]

        @JsonProperty("expirationDateTime")
        String expirationDateTime // Data/Hora de expiração no formato ISO 8601
    ) {}
}