package br.com.PersonalBank.service;

import br.com.PersonalBank.client.InvestmentsApiClient;
import br.com.PersonalBank.common.client.ApiClientProvider;
import br.com.PersonalBank.common.client.ConsentTokenClient;
import br.com.PersonalBank.common.event.InitialLoadEvent;
import br.com.PersonalBank.dto.investment.BankFixedIncome;
import br.com.PersonalBank.dto.investment.InvestmentFund;
import br.com.PersonalBank.dto.investment.VariableIncome;
import br.com.PersonalBank.entity.InvestmentHolding;
import br.com.PersonalBank.entity.InvestmentType;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InvestmentService {

    private static final Logger LOGGER = Logger.getLogger(InvestmentService.class);

    @Inject
    ApiClientProvider apiClientProvider;

    @Inject
    @RestClient
    ConsentTokenClient consentTokenClient;

    /**
     * Busca as posições de renda fixa bancária, fundos e renda variável do
     * consentimento e grava tudo. As chamadas HTTP acontecem antes, fora da
     * transação do banco de dados.
     */
    public void performInitialLoad(InitialLoadEvent event) {
        String institutionKey = event.institutionKey();
        String consentId = event.consentId();

        Optional<InvestmentsApiClient> clientOptional = apiClientProvider.buildClient(
                InvestmentsApiClient.class,
                institutionKey,
                "investments");
        if (clientOptional.isEmpty()) {
            LOGGER.warnf(
                    "API de Investimentos não configurada para a instituição [%s]. A carga inicial será ignorada.",
                    institutionKey);
            return;
        }

        InvestmentsApiClient client = clientOptional.get();
        String authorization = "Bearer " + consentTokenClient.getAccessToken(consentId);
        LOGGER.infof("Iniciando carga inicial de investimentos do consentimento [%s] no banco [%s]", consentId,
                institutionKey);

        List<BankFixedIncome> fixedIncomes = client.listBankFixedIncomes(authorization).data;
        List<InvestmentFund> funds = client.listFunds(authorization).data;
        List<VariableIncome> variableIncomes = client.listVariableIncomes(authorization).data;

        QuarkusTransaction.requiringNew().run(() -> {
            fixedIncomes.forEach(item -> InvestmentHolding.upsert(consentId, item.investmentId(),
                    InvestmentType.BANK_FIXED_INCOME, item.productName(), null, item.grossAmount()));
            funds.forEach(item -> InvestmentHolding.upsert(consentId, item.investmentId(),
                    InvestmentType.INVESTMENT_FUNDS, item.fundName(), item.quantity(), item.netAmount()));
            variableIncomes.forEach(item -> InvestmentHolding.upsert(consentId, item.investmentId(),
                    InvestmentType.VARIABLE_INCOME, item.ticker(), item.quantity(), item.currentValue()));
        });

        LOGGER.infof("Carga inicial de investimentos do consentimento [%s] concluída: %d posições.", consentId,
                fixedIncomes.size() + funds.size() + variableIncomes.size());
    }
}
