package br.com.PersonalBank.consumer;

import br.com.PersonalBank.event.UpdateCardEvent;
import br.com.PersonalBank.service.CreditCardService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class UpdateCardConsumer {

    @Inject
    CreditCardService creditCardService;

    @Incoming("atualizacoes-cartoes-bancos")
    public Uni<Void> process(UpdateCardEvent event) {
        // A lógica de atualização seria chamada aqui
        // return creditCardService.performUpdate(event);
        System.out.println("Recebido evento de atualização para conta: " + event.accountId());
        return Uni.createFrom().voidItem();
    }
}