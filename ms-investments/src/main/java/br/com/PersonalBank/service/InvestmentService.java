package br.com.PersonalBank.service;

import br.com.PersonalBank.client.InvestmentsApiClient;
import br.com.PersonalBank.common.client.ApiClientProvider;
import br.com.PersonalBank.common.event.InitialLoadEvent;
import br.com.PersonalBank.event.UpdateInvestmentEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import java.util.Optional;

@ApplicationScoped
public class InvestmentService {

    private static final Logger LOGGER = Logger.getLogger(InvestmentService.class);

    @Inject
    ApiClientProvider apiClientProvider;

    public Uni<Void> performInitialLoad(InitialLoadEvent event) {
        String institutionKey = event.institutionKey();
        String accountId = event.accountId();

        // ** A MUDANÇA PRINCIPAL ACONTECE AQUI **
        // 1. Pede o cliente e recebe um Optional
        Optional<InvestmentsApiClient> clientOptional = apiClientProvider.buildClient(
                InvestmentsApiClient.class,
                institutionKey,
                "investments");

        // 2. Verifica se o cliente foi criado com sucesso
        if (clientOptional.isEmpty()) {
            LOGGER.warnf(
                    "API de Investimentos não configurada para a instituição [%s]. A solicitação de carga inicial será ignorada.",
                    institutionKey);
            // Retorna um Uni completo, finalizando o processamento da mensagem com sucesso.
            return Uni.createFrom().voidItem();
        }

        // 3. Se o cliente existe, o fluxo normal continua
        InvestmentsApiClient client = clientOptional.get();
        String accessToken = "Bearer " + event.userToken();
        LOGGER.infof("Iniciando carga inicial de investimentos para conta [%s] no banco [%s]", accountId,
                institutionKey);

        return client.getConsolidatedPosition(accountId, accessToken)
                .onItem().invoke(response -> {
                    LOGGER.infof("Posição consolidada de investimentos para a conta [%s] obtida com sucesso.",
                            accountId);
                    // ... lógica de salvar no banco ...
                })
                .onFailure().invoke(failure -> {
                    LOGGER.errorf(failure, "Falha na carga inicial de investimentos para a conta [%s]", accountId);
                })
                .replaceWithVoid();
    }

    // O método performUpdate() seguiria a mesma lógica de verificação
    public Uni<Void> performUpdate(UpdateInvestmentEvent event) {
        // A mesma lógica de verificação com Optional seria aplicada aqui
        LOGGER.infof("Solicitação de atualização para [%s] no banco [%s] recebida.", event.type(),
                event.institutionKey());
        // ...
        return Uni.createFrom().voidItem();
    }
}