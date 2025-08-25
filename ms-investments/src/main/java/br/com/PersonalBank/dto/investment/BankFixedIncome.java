package br.com.PersonalBank.dto.investment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para produtos de Renda Fixa emitidos por bancos (CDB, LCI, LCA).
 */
public record BankFixedIncome(

        @JsonProperty("investmentId") String investmentId,

        @JsonProperty("productName") String productName, // Ex: "CDB LIQUIDEZ DIARIA"

        @JsonProperty("issuerName") String issuerName, // Nome da instituição emissora

        @JsonProperty("remunerationRate") BigDecimal remunerationRate, // Ex: 1.10 para 110%

        @JsonProperty("rateIndexer") String rateIndexer, // Ex: "CDI"

        @JsonProperty("maturityDate") LocalDate maturityDate, // Data de vencimento

        @JsonProperty("investedAmount") BigDecimal investedAmount, // Valor investido

        @JsonProperty("grossAmount") BigDecimal grossAmount // Valor bruto atual
) {
}