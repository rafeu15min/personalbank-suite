package br.com.PersonalBank.consumer;

import br.com.PersonalBank.common.event.InitialLoadEvent;
import br.com.PersonalBank.service.InvestmentService;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/**
 * Consumidor Kafka responsável por iniciar a carga inicial dos investimentos
 * de um consentimento recém-autorizado.
 */
@ApplicationScoped
public class InitialInvestmentLoadConsumer {

    @Inject
    InvestmentService investmentService;

    @Incoming("carga-inicial-investimentos")
    @Blocking
    public void process(InitialLoadEvent event) {
        investmentService.performInitialLoad(event);
    }
}
