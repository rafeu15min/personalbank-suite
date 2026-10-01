package br.com.PersonalBank.common.event;

/**
 * Evento publicado pelo ms-consent quando um consentimento é autorizado.
 * Sinaliza aos serviços de dados (transactions, credit-cards, investments)
 * que eles devem fazer a carga inicial daquele consentimento.
 *
 * O token de acesso não viaja no evento: cada serviço o pede ao ms-consent
 * pelo ConsentTokenClient, para que o token não fique gravado no Kafka.
 */
public record InitialLoadEvent(
        String consentId,
        String institutionKey) {
}
