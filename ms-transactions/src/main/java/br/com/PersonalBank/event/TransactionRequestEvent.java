package br.com.PersonalBank.event;

/**
 * Evento que solicita a busca de transações.
 * Convertido para um Record para imutabilidade e código mais limpo.
 */
public record TransactionRequestEvent(
        String accountId,
        String userToken,
        String institutionKey // A chave que identifica o banco
) {
}