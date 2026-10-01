package br.com.PersonalBank.client;

import br.com.PersonalBank.common.dto.OpenFinanceResponse;
import br.com.PersonalBank.dto.investment.BankFixedIncome;
import br.com.PersonalBank.dto.investment.InvestmentFund;
import br.com.PersonalBank.dto.investment.VariableIncome;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente das APIs de investimentos. No Open Finance cada tipo de produto tem
 * a sua própria API, todas abaixo da URL da família "investments" no banks.yml.
 */
@RegisterRestClient
@Produces(MediaType.APPLICATION_JSON)
public interface InvestmentsApiClient {

    @GET
    @Path("/bank-fixed-incomes/v1/investments")
    OpenFinanceResponse<List<BankFixedIncome>> listBankFixedIncomes(
            @HeaderParam("Authorization") String authorizationHeader);

    @GET
    @Path("/funds/v1/investments")
    OpenFinanceResponse<List<InvestmentFund>> listFunds(
            @HeaderParam("Authorization") String authorizationHeader);

    @GET
    @Path("/variable-incomes/v1/investments")
    OpenFinanceResponse<List<VariableIncome>> listVariableIncomes(
            @HeaderParam("Authorization") String authorizationHeader);
}
