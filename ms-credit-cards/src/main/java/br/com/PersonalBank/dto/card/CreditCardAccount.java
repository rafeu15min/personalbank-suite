package br.com.PersonalBank.dto.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * DTO para os dados da conta de um cartão de crédito.
 */
public record CreditCardAccount(
        @JsonProperty("creditCardAccountId") String creditCardAccountId,

        @JsonProperty("brandName") String brandName, // Ex: "MASTERCARD", "VISA"

        @JsonProperty("name") String name, // Ex: "Platinum"

        @JsonProperty("productType") String productType, // Ex: "CARTAO_CREDITO"

        @JsonProperty("availableCreditLimit") BigDecimal availableCreditLimit, // Limite disponível

        @JsonProperty("totalCreditLimit") BigDecimal totalCreditLimit // Limite total
) {
}