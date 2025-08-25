package br.com.PersonalBank.event;

/**
 * Enum que representa as diferentes categorias de produtos de investimento
 * que nosso sistema pode processar.
 */
public enum InvestmentType {
    BANK_FIXED_INCOME, // Renda Fixa Bancária (CDB, LCI, LCA)
    INVESTMENT_FUNDS, // Fundos de Investimento
    VARIABLE_INCOME // Renda Variável (Ações, BDRs, ETFs)
    // Adicione outros tipos conforme necessário
}