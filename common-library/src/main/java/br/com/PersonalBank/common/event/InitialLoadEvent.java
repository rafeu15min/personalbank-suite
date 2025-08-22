package br.com.PersonalBank.common.event;

/**
 * Evento genérico que sinaliza a necessidade de uma carga inicial de dados
 * para uma conta ou produto recém-vinculado por um usuário.
 *
 * Esta classe vive na 'common-library' para ser reutilizada por múltiplos
 * microsserviços consumidores (transactions, credit-cards, investments) e
 * por qualquer serviço produtor de eventos (ex: um futuro ms-consentimento).
 */
public record InitialLoadEvent(
        String accountId,
        String userToken,
        String institutionKey) {
}