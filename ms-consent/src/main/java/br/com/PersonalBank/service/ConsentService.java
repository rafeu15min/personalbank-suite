package br.com.PersonalBank.service;

import br.com.PersonalBank.client.BankAuthApiClient;
import br.com.PersonalBank.common.client.ApiClientProvider;
import br.com.PersonalBank.dto.InitiateConsentResponse;
import br.com.PersonalBank.dto.auth.CreateConsentRequest;
import br.com.PersonalBank.dto.auth.TokenResponse;
import br.com.PersonalBank.entity.UserConsent;
import br.com.PersonalBank.producer.ConsentEventProducer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ConsentService {

    private static final Logger LOGGER = Logger.getLogger(ConsentService.class);

    @Inject
    ApiClientProvider apiClientProvider;

    @Inject
    TokenService tokenService;

    @Inject
    ConsentEventProducer eventProducer;

    /**
     * Inicia o fluxo de consentimento com um banco.
     */
    @Transactional
    public Uni<InitiateConsentResponse> initiateConsentFlow(String institutionKey) {
        LOGGER.infof("Iniciando fluxo de consentimento para a instituição: %s", institutionKey);

        // Obtém o cliente de autenticação para o banco específico.
        // O Optional.orElseThrow() é usado aqui porque, se o banco não estiver configurado,
        // o fluxo não pode continuar.
        BankAuthApiClient authClient = apiClientProvider.buildClient(BankAuthApiClient.class, institutionKey, "auth") // Supondo uma família 'auth' no banks.yml
                .orElseThrow(() -> new IllegalStateException("API de autenticação não configurada para: " + institutionKey));

        // Cria o corpo da requisição de consentimento
        var permissions = List.of("ACCOUNTS_READ", "TRANSACTIONS_READ", "CREDIT_CARDS_ACCOUNTS_READ", "RESOURCES_READ");
        var expiration = LocalDateTime.now().plus(90, ChronoUnit.DAYS).toString();
        var requestBody = new CreateConsentRequest(new CreateConsentRequest.Data(permissions, expiration));

        // Chama a API do banco para criar o consentimento
        return authClient.createConsent(requestBody, "Bearer TOKEN_INICIAL") // O token inicial pode ser obtido via client_credentials
                .onItem().invoke(response -> {
                    // Após criar o consentimento no banco, criamos um registro no nosso DB para rastreá-lo.
                    UserConsent consent = new UserConsent();
                    consent.userId = UUID.randomUUID(); // Em um app real, viria do usuário logado
                    consent.institutionKey = institutionKey;
                    consent.consentId = response.data().consentId();
                    consent.status = response.data().status();
                    consent.encryptedAccessToken = "PENDING"; // Ainda não temos os tokens
                    consent.encryptedRefreshToken = "PENDING";
                    consent.accessTokenExpiry = LocalDateTime.now();
                    consent.persist(); // Salva no banco de dados
                    LOGGER.infof("Registro de consentimento [%s] criado localmente com status PENDENTE.", consent.consentId);
                })
                .onItem().transform(response -> {
                    // Retorna os dados necessários para o frontend
                    String redirectUrl = response.links().self(); // A URL de redirecionamento vem do banco
                    return new InitiateConsentResponse(response.data().consentId(), redirectUrl);
                });
    }

    /**
     * Finaliza o fluxo, trocando o código de autorização pelos tokens de acesso.
     */
    @Transactional
    public Uni<Void> exchangeCodeForTokens(String code, String consentId) {
        LOGGER.infof("Recebido callback para o consentimento: %s", consentId);

        // Encontra o registro de consentimento que criamos no passo anterior
        UserConsent consent = UserConsent.findByConsentId(consentId)
                .orElseThrow(() -> new IllegalStateException("Consentimento não encontrado: " + consentId));

        BankAuthApiClient authClient = apiClientProvider.buildClient(BankAuthApiClient.class, consent.institutionKey, "auth")
                .orElseThrow(() -> new IllegalStateException("API de autenticação não configurada para: " + consent.institutionKey));

        // Chama a API do banco para trocar o código pelo token
        return authClient.exchangeCodeForToken("authorization_code", code, "https://sua.app/callback", "seu-client-id")
                .onItem().invoke(tokenResponse -> {
                    LOGGER.infof("Tokens recebidos com sucesso para o consentimento: %s", consentId);
                    
                    // Criptografa e atualiza a nossa entidade de consentimento com os tokens
                    consent.encryptedAccessToken = tokenService.encrypt(tokenResponse.accessToken());
                    consent.encryptedRefreshToken = tokenService.encrypt(tokenResponse.refreshToken());
                    consent.accessTokenExpiry = LocalDateTime.now().plusSeconds(tokenResponse.expiresIn());
                    consent.status = "AUTHORISED";
                    // consent.persist() é chamado automaticamente pela transação

                    // Publica o evento no Kafka para os outros serviços começarem a buscar os dados
                    eventProducer.sendInitialLoadEvent(consent, tokenResponse.accessToken());
                })
                .replaceWithVoid();
    }
}