package br.com.PersonalBank.consumer;

import br.com.PersonalBank.common.event.InitialLoadEvent;
import br.com.PersonalBank.service.CreditCardService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class InitialCardLoadConsumer {

    @Inject
    CreditCardService creditCardService;

    @Incoming("carga-inicial-cartoes")
    public Uni<Void> process(InitialLoadEvent event) {
        // Apenas delega o trabalho pesado para a camada de serviço
        return creditCardService.performInitialLoad(event);
    }
}