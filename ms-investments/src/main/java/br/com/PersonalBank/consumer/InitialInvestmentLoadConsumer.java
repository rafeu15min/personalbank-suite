package br.com.PersonalBank.consumer;

import br.com.PersonalBank.common.event.InitialLoadEvent;
import br.com.PersonalBank.service.InvestmentService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/**
 * Consumidor Kafka responsável por iniciar o processo de carga inicial (bulk
 * load)
 * dos dados de investimentos de um usuário.
 */
@ApplicationScoped
public class InitialInvestmentLoadConsumer {

    @Inject
    InvestmentService investmentService; // Injeta a camada de serviço com a lógica de negócio

    /**
     * Este método é ativado automaticamente sempre que uma mensagem chega no tópico
     * configurado para o canal 'carga-inicial-investimentos'.
     *
     * @param event O evento genérico de carga inicial, vindo da common-library.
     * @return Um Uni<Void> para indicar que a mensagem foi recebida e o
     *         processamento
     *         reativo foi iniciado.
     */
    @Incoming("carga-inicial-investimentos")
    public Uni<Void> process(InitialLoadEvent event) {
        // Apenas delega o trabalho pesado para a camada de serviço,
        // mantendo o consumidor simples e focado em sua responsabilidade.
        return investmentService.performInitialLoad(event);
    }
}