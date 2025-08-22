package br.com.PersonalBank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO que representa uma única transação financeira.
 * Refatorado para usar Record, BigDecimal para valores monetários e LocalDate
 * para datas.
 */
public record Transaction(
        @JsonProperty("transactionId") String transactionId,

        @JsonProperty("completedAuthorisedPaymentType") String completedAuthorisedPaymentType,

        @JsonProperty("creditDebitType") String creditDebitType,

        @JsonProperty("type") String type,

        @JsonProperty("amount") BigDecimal amount, // BigDecimal é o tipo correto e seguro para dinheiro

        @JsonProperty("transactionCurrency") String transactionCurrency,

        @JsonProperty("transactionDate") LocalDate transactionDate, // LocalDate é o tipo correto para datas sem horário

        @JsonProperty("partieCounterpartyBrandName") String partieCounterpartyBrandName,

        @JsonProperty("partieCounterpartyName") String partieCounterpartyName) {
}