package br.com.PersonalBank.service;

import br.com.PersonalBank.client.BankAuthApiClient;
import br.com.PersonalBank.client.ConsentApiClient;
import br.com.PersonalBank.common.client.ApiClientProvider;
import br.com.PersonalBank.common.config.InstitutionConfigProvider;
import br.com.PersonalBank.dto.InitiateConsentResponse;
import br.com.PersonalBank.dto.auth.CreateConsentRequest;
import br.com.PersonalBank.dto.auth.CreateConsentResponse;
import br.com.PersonalBank.dto.auth.TokenResponse;
import br.com.PersonalBank.entity.UserConsent;
import br.com.PersonalBank.producer.ConsentEventProducer;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Orquestra o fluxo de consentimento do Open Finance:
 * 1. initiateConsentFlow: obtém um token da aplicação, cria o consentimento no
 *    banco e devolve a URL para onde o usuário deve ser redirecionado.
 * 2. completeAuthorisation: no callback, troca o código pelos tokens do usuário,
 *    guarda-os criptografados e publica o evento de carga inicial.
 *
 * As chamadas HTTP ao banco ficam fora das transações do banco de dados.
 */
@ApplicationScoped
public class ConsentService {

    private static final Logger LOGGER = Logger.getLogger(ConsentService.class);

    public static final String AUTHORISED = "AUTHORISED";

    private static final List<String> PERMISSIONS = List.of(
            "ACCOUNTS_READ", "ACCOUNTS_TRANSACTIONS_READ",
            "CREDIT_CARDS_ACCOUNTS_READ", "CREDIT_CARDS_ACCOUNTS_BILLS_READ",
            "CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ",
            "BANK_FIXED_INCOMES_READ", "FUNDS_READ", "VARIABLE_INCOMES_READ",
            "RESOURCES_READ");

    @Inject
    ApiClientProvider apiClientProvider;

    @Inject
    InstitutionConfigProvider institutions;

    @Inject
    TokenService tokenService;

    @Inject
    ConsentEventProducer eventProducer;

    @ConfigProperty(name = "personalbank.consent.client-id")
    String clientId;

    @ConfigProperty(name = "personalbank.consent.redirect-uri")
    String redirectUri;

    public InitiateConsentResponse initiateConsentFlow(String institutionKey) {
        LOGGER.infof("Iniciando fluxo de consentimento para a instituição: %s", institutionKey);

        BankAuthApiClient authClient = client(BankAuthApiClient.class, institutionKey, "auth");
        ConsentApiClient consentClient = client(ConsentApiClient.class, institutionKey, "consents");
        String authorizeUrl = institutions.getApiUrl(institutionKey, "authorize")
                .orElseThrow(() -> notConfigured(institutionKey, "authorize"));

        TokenResponse appToken = authClient.requestToken("client_credentials", clientId);
        String expiration = OffsetDateTime.now(ZoneOffset.UTC).plusDays(90).truncatedTo(ChronoUnit.SECONDS).toString();
        CreateConsentResponse created = consentClient.createConsent(
                new CreateConsentRequest(new CreateConsentRequest.Data(PERMISSIONS, expiration)),
                "Bearer " + appToken.accessToken());

        UserConsent consent = new UserConsent();
        consent.userId = UUID.randomUUID(); // Ainda não há cadastro de usuários
        consent.institutionKey = institutionKey;
        consent.consentId = created.data().consentId();
        consent.status = created.data().status();
        QuarkusTransaction.requiringNew().run(consent::persist);
        LOGGER.infof("Consentimento [%s] criado com status %s.", consent.consentId, consent.status);

        // O state volta no callback e identifica o consentimento. É o UUID interno,
        // que não dá para adivinhar, o que também protege o callback contra CSRF.
        String redirectUrl = UriBuilder.fromUri(authorizeUrl)
                .queryParam("client_id", clientId)
                .queryParam("consent_id", consent.consentId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", consent.id)
                .build()
                .toString();
        return new InitiateConsentResponse(consent.consentId, redirectUrl);
    }

    public UserConsent completeAuthorisation(String code, String state) {
        UUID id = parseState(state);
        UserConsent consent = UserConsent.<UserConsent>findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Consentimento não encontrado"));
        LOGGER.infof("Recebido callback para o consentimento: %s", consent.consentId);

        BankAuthApiClient authClient = client(BankAuthApiClient.class, consent.institutionKey, "auth");
        TokenResponse tokens = authClient.exchangeCode("authorization_code", code, redirectUri, clientId);

        UserConsent authorised = QuarkusTransaction.requiringNew().call(() -> {
            UserConsent managed = UserConsent.findById(id);
            managed.encryptedAccessToken = tokenService.encrypt(tokens.accessToken());
            managed.encryptedRefreshToken = tokenService.encrypt(tokens.refreshToken());
            managed.accessTokenExpiry = LocalDateTime.now().plusSeconds(tokens.expiresIn());
            managed.status = AUTHORISED;
            return managed;
        });
        LOGGER.infof("Consentimento [%s] autorizado.", authorised.consentId);

        // Publica só depois do commit: os serviços de dados pedem o token logo em seguida.
        eventProducer.sendInitialLoadEvent(authorised);
        return authorised;
    }

    /**
     * Token de acesso em texto plano, para uso dos serviços de dados.
     */
    @Transactional
    public String getAccessToken(String consentId) {
        UserConsent consent = UserConsent.findByConsentId(consentId)
                .filter(c -> AUTHORISED.equals(c.status))
                .orElseThrow(() -> new NotFoundException("Consentimento autorizado não encontrado"));
        if (consent.accessTokenExpiry.isBefore(LocalDateTime.now())) {
            // A renovação com o refresh_token ainda não foi implementada.
            throw new WebApplicationException("Token expirado", Response.Status.CONFLICT);
        }
        return tokenService.decrypt(consent.encryptedAccessToken);
    }

    public static BadRequestException badRequest(String message) {
        return new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", message))
                .build());
    }

    private <T> T client(Class<T> clientInterface, String institutionKey, String apiFamily) {
        return apiClientProvider.buildClient(clientInterface, institutionKey, apiFamily)
                .orElseThrow(() -> notConfigured(institutionKey, apiFamily));
    }

    private static BadRequestException notConfigured(String institutionKey, String apiFamily) {
        return badRequest("Instituição sem a API '" + apiFamily + "' configurada: " + institutionKey);
    }

    private static UUID parseState(String state) {
        try {
            return UUID.fromString(state);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw badRequest("Parâmetro state inválido");
        }
    }
}
