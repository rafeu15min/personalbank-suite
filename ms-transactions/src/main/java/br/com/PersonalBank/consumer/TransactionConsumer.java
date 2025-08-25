package br.com.PersonalBank.consumer;

import br.com.PersonalBank.common.event.InitialLoadEvent;
import br.com.PersonalBank.service.TransactionService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/**
 * Consumidor Kafka responsável por receber solicitações para buscar transações.
 * Esta classe atua como a camada de entrada (entrypoint) para o fluxo de
 * negócio.
 */
@ApplicationScoped
public class TransactionConsumer {

    @Inject
    TransactionService transactionService; // Injeta a camada de serviço que contém a lógica de negócio

    /**
     * Este método é ativado automaticamente sempre que uma mensagem chega no tópico
     * configurado para o canal 'solicitacoes-transacoes'.
     *
     * @param event O evento que contém os dados da solicitação (accountId, token,
     *              etc.).
     * @return Um Uni<Void> que representa a conclusão da operação assíncrona.
     */
    @Incoming("solicitacoes-transacoes")
    public Uni<Void> process(InitialLoadEvent event) {
        // A única responsabilidade do consumidor é validar e delegar.
        // Todo o trabalho pesado (criar cliente, chamar API, tratar erros)
        // é feito pela camada de serviço.
        return transactionService.fetchAndProcessTransactions(event);
    }
}