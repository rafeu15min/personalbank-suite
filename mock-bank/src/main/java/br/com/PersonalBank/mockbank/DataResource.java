package br.com.PersonalBank.mockbank;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * APIs de dados simuladas. Todo consentimento autorizado enxerga os mesmos
 * dados fictícios, que ficam em MockData.
 */
@Path("/open-banking")
@Produces(MediaType.APPLICATION_JSON)
public class DataResource {

    @Inject
    MockBankState state;

    @GET
    @Path("accounts/v2/accounts")
    public Map<String, Object> accounts(@HeaderParam("Authorization") String authorization) {
        state.requireAccessToken(authorization);
        return page(MockData.ACCOUNTS);
    }

    @GET
    @Path("accounts/v2/accounts/{accountId}/transactions")
    public Map<String, Object> accountTransactions(
            @PathParam("accountId") String accountId, @HeaderParam("Authorization") String authorization) {
        state.requireAccessToken(authorization);
        return page(lookup(MockData.ACCOUNT_TRANSACTIONS, accountId));
    }

    @GET
    @Path("credit-cards-accounts/v2/accounts")
    public Map<String, Object> cardAccounts(@HeaderParam("Authorization") String authorization) {
        state.requireAccessToken(authorization);
        return page(MockData.CARD_ACCOUNTS);
    }

    @GET
    @Path("credit-cards-accounts/v2/accounts/{accountId}/bills")
    public Map<String, Object> cardBills(
            @PathParam("accountId") String accountId, @HeaderParam("Authorization") String authorization) {
        state.requireAccessToken(authorization);
        return page(lookup(MockData.CARD_BILLS, accountId));
    }

    @GET
    @Path("credit-cards-accounts/v2/accounts/{accountId}/transactions")
    public Map<String, Object> cardTransactions(
            @PathParam("accountId") String accountId, @HeaderParam("Authorization") String authorization) {
        state.requireAccessToken(authorization);
        return page(lookup(MockData.CARD_TRANSACTIONS, accountId));
    }

    @GET
    @Path("bank-fixed-incomes/v1/investments")
    public Map<String, Object> bankFixedIncomes(@HeaderParam("Authorization") String authorization) {
        state.requireAccessToken(authorization);
        return page(MockData.BANK_FIXED_INCOMES);
    }

    @GET
    @Path("funds/v1/investments")
    public Map<String, Object> funds(@HeaderParam("Authorization") String authorization) {
        state.requireAccessToken(authorization);
        return page(MockData.FUNDS);
    }

    @GET
    @Path("variable-incomes/v1/investments")
    public Map<String, Object> variableIncomes(@HeaderParam("Authorization") String authorization) {
        state.requireAccessToken(authorization);
        return page(MockData.VARIABLE_INCOMES);
    }

    private static <T> List<T> lookup(Map<String, List<T>> byAccount, String accountId) {
        List<T> items = byAccount.get(accountId);
        if (items == null) {
            throw new NotFoundException();
        }
        return items;
    }

    /** Envelope padrão das respostas do Open Finance (data, links, meta), sem paginação real. */
    private static Map<String, Object> page(List<?> data) {
        return Map.of(
                "data", data,
                "links", Map.of(),
                "meta", Map.of("totalRecords", data.size(), "totalPages", 1));
    }
}
