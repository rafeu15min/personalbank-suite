package br.com.PersonalBank.dto.investment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para posições em Fundos de Investimento.
 */
public record InvestmentFund(

        @JsonProperty("investmentId") String investmentId,

        @JsonProperty("fundName") String fundName, // Nome do fundo

        @JsonProperty("fundCnpj") String fundCnpj, // CNPJ do fundo

        @JsonProperty("quantity") BigDecimal quantity, // Quantidade de cotas

        @JsonProperty("unitPrice") BigDecimal unitPrice, // Valor da cota

        @JsonProperty("updatedAt") LocalDateTime updatedAt, // Data e hora da atualização da cota

        @JsonProperty("netAmount") BigDecimal netAmount // Valor líquido atual
) {
}