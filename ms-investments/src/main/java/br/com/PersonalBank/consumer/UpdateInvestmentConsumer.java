package br.com.PersonalBank.consumer;

import br.com.PersonalBank.event.UpdateInvestmentEvent;
import br.com.PersonalBank.service.InvestmentService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/**
 * Consumidor Kafka responsável por processar as solicitações de atualização
 * periódica dos dados de investimentos.
 */
@ApplicationScoped
public class UpdateInvestmentConsumer {

    @Inject
    InvestmentService investmentService; // Injeta a mesma camada de serviço

    /**
     * Este método é ativado para mensagens que chegam em qualquer tópico que
     * corresponda ao padrão 'investimentos.atualizacoes.*', conforme configurado
     * no application.properties.
     *
     * @param event O evento específico para atualização de investimentos, que
     *              contém o tipo.
     * @return Um Uni<Void> para indicar que o processamento foi iniciado.
     */
    @Incoming("atualizacoes-investimentos-bancos")
    public Uni<Void> process(UpdateInvestmentEvent event) {
        // Delega o trabalho de atualização para a camada de serviço.
        return investmentService.performUpdate(event);
    }
}