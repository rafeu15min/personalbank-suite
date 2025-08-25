package br.com.PersonalBank.dto.investment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para posições em Renda Variável (Ações, BDRs, ETFs, etc.).
 */
public record VariableIncome(

        @JsonProperty("investmentId") String investmentId,

        @JsonProperty("ticker") String ticker, // Código de negociação do ativo. Ex: "PETR4"

        @JsonProperty("assetType") String assetType, // Tipo do ativo. Ex: "AÇÃO"

        @JsonProperty("quantity") BigDecimal quantity, // Quantidade de ativos

        @JsonProperty("closingPrice") BigDecimal closingPrice, // Preço de fechamento do dia de referência

        @JsonProperty("referenceDate") LocalDate referenceDate, // Data de referência para o preço

        @JsonProperty("currentValue") BigDecimal currentValue // Valor total atual (quantidade * preço)
) {
}