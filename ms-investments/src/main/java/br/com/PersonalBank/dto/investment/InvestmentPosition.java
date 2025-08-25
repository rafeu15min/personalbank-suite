package br.com.PersonalBank.dto.investment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO que representa a posição consolidada de investimentos de um cliente.
 * Atua como um container para as diferentes categorias de produtos.
 */
public record InvestmentPosition(

        @JsonProperty("bankFixedIncomes") List<BankFixedIncome> bankFixedIncomes,

        @JsonProperty("investmentFunds") List<InvestmentFund> investmentFunds,

        @JsonProperty("variableIncomes") List<VariableIncome> variableIncomes

// Adicione outras listas para diferentes tipos de produtos conforme necessário
// Ex: List<TreasuryFixedIncome> treasuryFixedIncomes;

) {
}