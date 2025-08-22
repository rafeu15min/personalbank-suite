package br.com.PersonalBank.dto.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para os dados de uma fatura de cartão de crédito.
 */
public record CreditCardInvoice(
        @JsonProperty("invoiceId") String invoiceId,

        @JsonProperty("dueDate") LocalDate dueDate, // Data de vencimento

        @JsonProperty("closingDate") LocalDate closingDate, // Data de fechamento

        @JsonProperty("totalAmount") BigDecimal totalAmount, // Valor total da fatura

        @JsonProperty("totalAmountCurrency") String totalAmountCurrency, // Ex: "BRL"

        @JsonProperty("isInstalment") boolean isInstalment // Indica se é uma fatura de parcelamento
) {
}