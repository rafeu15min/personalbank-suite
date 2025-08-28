package br.com.PersonalBank.producer;

import br.com.PersonalBank.common.event.InitialLoadEvent;
import br.com.PersonalBank.entity.UserConsent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

/**
 * Responsável por produzir e enviar eventos para os tópicos do Kafka.
 * Atua como a ponte entre o fluxo de consentimento e os microsserviços de ingestão de dados.
 */
@ApplicationScoped
public class ConsentEventProducer {

    private static final Logger LOGGER = Logger.getLogger(ConsentEventProducer.class);

    // Injeta um "emissor" para o canal de saída configurado no application.properties
    @Inject
    @Channel("initial-load-out")
    Emitter<InitialLoadEvent> emitter;

    /**
     * Publica um evento de carga inicial após um consentimento bem-sucedido.
     *
     * @param consent A entidade de consentimento que acabou de ser autorizada.
     * @param accessToken O token de acesso decriptografado, pronto para ser usado.
     */
    public void sendInitialLoadEvent(UserConsent consent, String accessToken) {
        
        // Cria o evento com os dados necessários para os outros serviços
        var event = new InitialLoadEvent(
                consent.consentId, // Usamos o consentId como o accountId para consistência inicial
                accessToken,
                consent.institutionKey
        );

        // Envia a mensagem para o tópico Kafka configurado no canal 'initial-load-out'.
        // O Quarkus Reactive Messaging lida com a serialização para JSON.
        emitter.send(event);

        LOGGER.infof("Evento de carga inicial para consentimento [%s] enviado com sucesso.", consent.consentId);
    }
}