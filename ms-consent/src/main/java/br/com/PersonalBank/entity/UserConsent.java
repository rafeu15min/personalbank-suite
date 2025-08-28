package br.com.PersonalBank.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Entidade que representa um consentimento de usuário no banco de dados.
 * Mapeia a tabela 'user_consents'. Usamos PanacheEntityBase para ter
 * controle total sobre a chave primária (ID).
 */
@Entity
@Table(name = "user_consents")
public class UserConsent extends PanacheEntityBase {

    /**
     * Chave primária da nossa tabela, gerada pela aplicação.
     */
    @Id
    @Column(nullable = false)
    public UUID id;

    /**
     * ID do usuário no nosso sistema (ex: da sua tabela de usuários).
     */
    @Column(nullable = false, name = "user_id")
    public UUID userId;

    /**
     * Chave da instituição (ex: "NUBANK", "ITAU").
     */
    @Column(nullable = false, name = "institution_key")
    public String institutionKey;

    /**
     * O ID do consentimento retornado pelo banco.
     */
    @Column(nullable = false, unique = true, name = "consent_id")
    public String consentId;

    /**
     * O token de acesso, ARMAZENADO DE FORMA CRIPTOGRAFADA.
     * A coluna é definida como 'text' para acomodar o tamanho do token criptografado.
     */
    @Column(nullable = false, columnDefinition = "TEXT", name = "encrypted_access_token")
    public String encryptedAccessToken;

    /**
     * O token de atualização, ARMAZENADO DE FORMA CRIPTOGRAFADA.
     */
    @Column(nullable = false, columnDefinition = "TEXT", name = "encrypted_refresh_token")
    public String encryptedRefreshToken;

    /**
     * Data e hora em que o token de acesso atual irá expirar.
     * Usado para saber quando precisamos usar o refresh_token.
     */
    @Column(nullable = false, name = "access_token_expiry")
    public LocalDateTime accessTokenExpiry;

    /**
     * Status atual do consentimento.
     * Ex: "AWAITING_AUTHORISATION", "AUTHORISED", "REVOKED"
     */
    @Column(nullable = false)
    public String status;

    /**
     * Data de criação do registro no nosso banco de dados.
     */
    @Column(nullable = false, name = "created_at")
    public LocalDateTime createdAt;


    // --- Métodos Utilitários do Panache ---

    /**
     * Método estático para encontrar um consentimento pelo seu ID do Open Finance.
     *
     * @param consentId O ID do consentimento fornecido pelo banco.
     * @return Um Optional contendo a entidade UserConsent, se encontrada.
     */
    public static Optional<UserConsent> findByConsentId(String consentId) {
        return find("consentId", consentId).firstResultOptional();
    }

    /**
     * Construtor padrão.
     */
    public UserConsent() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }
}