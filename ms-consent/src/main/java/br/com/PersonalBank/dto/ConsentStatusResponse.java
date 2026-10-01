package br.com.PersonalBank.dto;

/**
 * Resposta do callback de consentimento.
 */
public record ConsentStatusResponse(
    String consentId,
    String institutionKey,
    String status
) {}
