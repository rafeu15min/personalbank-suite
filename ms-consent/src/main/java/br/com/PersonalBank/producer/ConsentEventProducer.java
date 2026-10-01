package br.com.PersonalBank.producer;

import br.com.PersonalBank.common.event.InitialLoadEvent;
import br.com.PersonalBank.entity.UserConsent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

/**
 * Publica no Kafka os consentimentos autorizados. Cada serviço de dados
 * (transactions, credit-cards, investments) consome o mesmo tópico com seu
 * próprio consumer group, então todos recebem o evento.
 */
@ApplicationScoped
public class ConsentEventProducer {

    private static final Logger LOGGER = Logger.getLogger(ConsentEventProducer.class);

    @Inject
    @Channel("consentimentos-autorizados")
    Emitter<InitialLoadEvent> emitter;

    public void sendInitialLoadEvent(UserConsent consent) {
        emitter.send(new InitialLoadEvent(consent.consentId, consent.institutionKey));
        LOGGER.infof("Evento de carga inicial para consentimento [%s] enviado.", consent.consentId);
    }
}
