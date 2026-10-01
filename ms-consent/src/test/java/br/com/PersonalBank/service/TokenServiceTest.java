package br.com.PersonalBank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class TokenServiceTest {

    private static String key(int fill) {
        byte[] raw = new byte[32];
        java.util.Arrays.fill(raw, (byte) fill);
        return Base64.getEncoder().encodeToString(raw);
    }

    private final TokenService service = new TokenService(key(1));

    @Test
    void roundTrip() {
        String encrypted = service.encrypt("at-123");
        assertNotEquals("at-123", encrypted);
        assertEquals("at-123", service.decrypt(encrypted));
    }

    @Test
    void sameTokenEncryptsDifferentlyEachTime() {
        assertNotEquals(service.encrypt("at-123"), service.encrypt("at-123"));
    }

    @Test
    void rejectsTamperedValue() {
        byte[] data = Base64.getDecoder().decode(service.encrypt("at-123"));
        data[data.length - 1] ^= 1;
        String tampered = Base64.getEncoder().encodeToString(data);
        assertThrows(IllegalStateException.class, () -> service.decrypt(tampered));
    }

    @Test
    void rejectsOtherKey() {
        String encrypted = service.encrypt("at-123");
        assertThrows(IllegalStateException.class, () -> new TokenService(key(2)).decrypt(encrypted));
    }

    @Test
    void rejectsKeyOfWrongSize() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);
        assertThrows(IllegalArgumentException.class, () -> new TokenService(shortKey));
    }
}
