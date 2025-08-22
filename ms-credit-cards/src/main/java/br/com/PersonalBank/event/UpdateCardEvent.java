package br.com.PersonalBank.event;

/**
 * Evento que solicita a verificação de novas faturas/transações para um cartão.
 */
public record UpdateCardEvent(
        String accountId,
        String userToken,
        String institutionKey) {
}