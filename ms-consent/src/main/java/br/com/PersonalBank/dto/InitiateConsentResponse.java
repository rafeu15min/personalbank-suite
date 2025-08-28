package br.com.PersonalBank.dto;

/**
 * DTO para a resposta do início de consentimento.
 * Contém a URL do banco para a qual o frontend deve redirecionar o usuário.
 */
public record InitiateConsentResponse(
    String consentId, // O ID do consentimento gerado
    String redirectUrl // A URL de autorização do banco
) {}