package br.com.PersonalBank.mockbank;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotAuthorizedException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Estado em memória do banco simulado: consentimentos, códigos de autorização
 * e tokens emitidos. Tudo se perde quando o serviço reinicia.
 */
@ApplicationScoped
public class MockBankState {

    public static final String AWAITING_AUTHORISATION = "AWAITING_AUTHORISATION";
    public static final String AUTHORISED = "AUTHORISED";

    /** consentId -> status */
    private final Map<String, String> consents = new ConcurrentHashMap<>();
    /** código de autorização -> consentId (cada código vale uma vez) */
    private final Map<String, String> authorizationCodes = new ConcurrentHashMap<>();
    /** access token -> consentId */
    private final Map<String, String> accessTokens = new ConcurrentHashMap<>();
    /** tokens client_credentials, que só servem para criar consentimentos */
    private final Set<String> clientTokens = ConcurrentHashMap.newKeySet();

    public String issueClientToken() {
        String token = "cc-" + UUID.randomUUID();
        clientTokens.add(token);
        return token;
    }

    public void requireClientToken(String authorizationHeader) {
        if (!clientTokens.contains(bearer(authorizationHeader))) {
            throw new NotAuthorizedException("Bearer");
        }
    }

    public String createConsent() {
        String consentId = "urn:mockbank:" + UUID.randomUUID();
        consents.put(consentId, AWAITING_AUTHORISATION);
        return consentId;
    }

    /**
     * Marca o consentimento como autorizado e devolve um código de autorização,
     * ou vazio se o consentimento não existir.
     */
    public Optional<String> authorise(String consentId) {
        if (consentId == null || consents.replace(consentId, AUTHORISED) == null) {
            return Optional.empty();
        }
        String code = UUID.randomUUID().toString();
        authorizationCodes.put(code, consentId);
        return Optional.of(code);
    }

    /** Troca o código por um access token, ou vazio se o código for inválido ou já usado. */
    public Optional<String> exchangeCode(String code) {
        return Optional.ofNullable(code == null ? null : authorizationCodes.remove(code))
                .map(consentId -> {
                    String token = "at-" + UUID.randomUUID();
                    accessTokens.put(token, consentId);
                    return token;
                });
    }

    /** Valida o token de um consentimento autorizado; responde 401 caso contrário. */
    public void requireAccessToken(String authorizationHeader) {
        String consentId = accessTokens.get(bearer(authorizationHeader));
        if (consentId == null || !AUTHORISED.equals(consents.get(consentId))) {
            throw new NotAuthorizedException("Bearer");
        }
    }

    private static String bearer(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return "";
        }
        return authorizationHeader.substring("Bearer ".length());
    }
}
