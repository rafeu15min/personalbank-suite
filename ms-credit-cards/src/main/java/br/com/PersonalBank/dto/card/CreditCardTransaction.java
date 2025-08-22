package br.com.PersonalBank.dto.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para os dados de uma transação (lançamento) dentro de uma fatura.
 */
public record CreditCardTransaction(
        @JsonProperty("transactionId") String transactionId,

        @JsonProperty("isInstalment") boolean isInstalment, // Lançamento de uma parcela

        @JsonProperty("instalmentNumber") int instalmentNumber, // Número da parcela (ex: 2 de 10)

        @JsonProperty("totalInstalments") int totalInstalments,

        @JsonProperty("description") String description, // Descrição da compra

        @JsonProperty("amount") BigDecimal amount, // Valor do lançamento

        @JsonProperty("transactionDate") LocalDate transactionDate, // Data da compra

        @JsonProperty("billId") String billId // ID da fatura à qual este lançamento pertence
) {
}