package br.com.PersonalBank.event;

/**
 * Evento que solicita a verificação de atualizações para uma categoria
 * específica de investimentos de um usuário.
 */
public record UpdateInvestmentEvent(
        String accountId,
        String userToken,
        String institutionKey,
        InvestmentType type // O campo que direciona qual tipo de produto verificar
) {}