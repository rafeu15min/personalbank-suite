package br.com.PersonalBank.dto;

/**
 * DTO (Data Transfer Object) para a requisição de início de consentimento.
 * Usado como o corpo (body) da chamada POST /api/consents/initiate.
 * É um record para simplicidade e imutabilidade.
 */
public record InitiateConsentRequest(
    String institutionKey // A chave do banco que o usuário selecionou (ex: "NUBANK", "ITAU")
) {}