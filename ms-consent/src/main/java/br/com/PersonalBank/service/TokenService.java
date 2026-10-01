package br.com.PersonalBank.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Criptografa os tokens dos bancos antes de irem para o banco de dados,
 * usando AES-256-GCM. O resultado é Base64(IV || texto cifrado + tag), com um
 * IV aleatório por token.
 *
 * A chave vem de personalbank.token-encryption-key (32 bytes em Base64).
 * Gere uma com: openssl rand -base64 32
 */
@Singleton
public class TokenService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    @Inject
    public TokenService(@ConfigProperty(name = "personalbank.token-encryption-key") String base64Key) {
        byte[] raw = Base64.getDecoder().decode(base64Key.trim());
        if (raw.length != 32) {
            throw new IllegalArgumentException(
                    "personalbank.token-encryption-key deve ter 32 bytes em Base64 (AES-256), tem " + raw.length);
        }
        this.key = new SecretKeySpec(raw, "AES");
    }

    public String encrypt(String plainToken) {
        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plainToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(
                    ByteBuffer.allocate(iv.length + ciphertext.length).put(iv).put(ciphertext).array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao criptografar token", e);
        }
    }

    /**
     * @throws IllegalStateException se o valor foi alterado ou cifrado com outra chave
     */
    public String decrypt(String encryptedToken) {
        byte[] data = Base64.getDecoder().decode(encryptedToken);
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, data, 0, IV_BYTES));
            byte[] plain = cipher.doFinal(data, IV_BYTES, data.length - IV_BYTES);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao decriptografar token", e);
        }
    }
}
