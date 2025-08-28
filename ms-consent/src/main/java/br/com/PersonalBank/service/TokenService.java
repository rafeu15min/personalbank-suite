package br.com.PersonalBank.service;

import jakarta.enterprise.context.ApplicationScoped;
// Importações para um cofre de segredos (ex: Vault) e criptografia seriam adicionadas aqui.

/**
 * Serviço de alta segurança dedicado exclusivamente ao gerenciamento de tokens.
 * A implementação real se conectaria a um cofre de segredos para obter a chave de criptografia.
 */
@ApplicationScoped
public class TokenService {

    // Em um cenário real, esta chave seria carregada de forma segura de um Vault na inicialização.
    private final String ENCRYPTION_KEY = "uma-chave-secreta-muito-forte";

    /**
     * Criptografa um token antes de salvá-lo no banco de dados.
     * @param plainToken O token em texto plano.
     * @return O token como uma string criptografada.
     */
    public String encrypt(String plainToken) {
        LOGGER.info("Criptografando token...");
        // LÓGICA DE CRIPTOGRAFIA (ex: usando AES-256) IRIA AQUI
        // Por simplicidade, vamos apenas retornar o token "invertido".
        return new StringBuilder(plainToken).reverse().toString();
    }

    /**
     * Decriptografa um token lido do banco de dados.
     * @param encryptedToken O token criptografado.
     * @return O token em texto plano, pronto para uso.
     */
    public String decrypt(String encryptedToken) {
        LOGGER.info("Decriptografando token...");
        // LÓGICA DE DECRIPTOGRAFIA (ex: usando AES-256) IRIA AQUI
        return new StringBuilder(encryptedToken).reverse().toString();
    }
}