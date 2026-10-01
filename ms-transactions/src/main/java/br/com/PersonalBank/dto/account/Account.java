package br.com.PersonalBank.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de uma conta (corrente, poupança ou pagamento pré-paga) do usuário.
 */
public record Account(
        @JsonProperty("accountId") String accountId,

        @JsonProperty("brandName") String brandName,

        @JsonProperty("type") String type, // Ex: "CONTA_DEPOSITO_A_VISTA", "CONTA_POUPANCA"

        @JsonProperty("number") String number) {
}
