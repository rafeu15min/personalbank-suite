package br.com.PersonalBank.consumer;

import br.com.PersonalBank.event.TransactionRequestEvent;
import br.com.PersonalBank.service.TransactionService;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TransactionConsumer {

    @Inject
    TransactionService transactionService;

    @Incoming("solicitacoes-transacoes")
    public Uni<Void> process(TransactionRequestEvent event) {
        // Apenas delega o processamento para a camada de serviço
        return transactionService.fetchAndProcessTransactions(event);
    }
}