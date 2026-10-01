package br.com.PersonalBank.consumer;

import br.com.PersonalBank.common.event.InitialLoadEvent;
import br.com.PersonalBank.service.TransactionService;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/**
 * Recebe os consentimentos autorizados e dispara a carga inicial de contas e
 * transações. @Blocking roda o método numa worker thread, já que ele faz
 * chamadas HTTP e acessa o banco de dados.
 */
@ApplicationScoped
public class TransactionConsumer {

    @Inject
    TransactionService transactionService;

    @Incoming("carga-inicial-transacoes")
    @Blocking
    public void process(InitialLoadEvent event) {
        transactionService.performInitialLoad(event);
    }
}
