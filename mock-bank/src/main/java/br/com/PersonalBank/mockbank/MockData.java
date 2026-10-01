package br.com.PersonalBank.mockbank;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Dados fictícios servidos pelo mock-bank. Os nomes dos campos seguem os DTOs
 * dos serviços de dados, que por sua vez são uma versão simplificada dos
 * schemas do Open Finance Brasil.
 */
public final class MockData {

    private MockData() {
    }

    public record Account(String accountId, String brandName, String type, String number) {
    }

    public record AccountTransaction(String transactionId, String completedAuthorisedPaymentType, String creditDebitType,
            String type, BigDecimal amount, String transactionCurrency, LocalDate transactionDate,
            String partieCounterpartyBrandName, String partieCounterpartyName) {
    }

    public record CardAccount(String creditCardAccountId, String brandName, String name, String productType,
            BigDecimal availableCreditLimit, BigDecimal totalCreditLimit) {
    }

    public record CardBill(String invoiceId, LocalDate dueDate, LocalDate closingDate, BigDecimal totalAmount,
            String totalAmountCurrency, @JsonProperty("isInstalment") boolean isInstalment) {
    }

    public record CardTransaction(String transactionId, @JsonProperty("isInstalment") boolean isInstalment,
            int instalmentNumber, int totalInstalments, String description, BigDecimal amount,
            LocalDate transactionDate, String billId) {
    }

    public record BankFixedIncome(String investmentId, String productName, String issuerName, BigDecimal remunerationRate,
            String rateIndexer, LocalDate maturityDate, BigDecimal investedAmount, BigDecimal grossAmount) {
    }

    public record Fund(String investmentId, String fundName, String fundCnpj, BigDecimal quantity, BigDecimal unitPrice,
            LocalDateTime updatedAt, BigDecimal netAmount) {
    }

    public record VariableIncome(String investmentId, String ticker, String assetType, BigDecimal quantity,
            BigDecimal closingPrice, LocalDate referenceDate, BigDecimal currentValue) {
    }

    static final List<Account> ACCOUNTS = List.of(
            new Account("acc-001", "Mock Bank", "CONTA_DEPOSITO_A_VISTA", "12345-6"),
            new Account("acc-002", "Mock Bank", "CONTA_POUPANCA", "98765-4"));

    static final Map<String, List<AccountTransaction>> ACCOUNT_TRANSACTIONS = Map.of(
            "acc-001", List.of(
                    tx("tx-001", "CREDITO", "TED", "5200.00", "2026-09-05", "ACME LTDA", "ACME Tecnologia Ltda"),
                    tx("tx-002", "DEBITO", "PIX", "1450.00", "2026-09-06", null, "Imobiliária Central"),
                    tx("tx-003", "DEBITO", "BOLETO", "189.90", "2026-09-10", "Vivo", "Telefônica Brasil S.A."),
                    tx("tx-004", "DEBITO", "PIX", "62.35", "2026-09-12", null, "Padaria Pão Quente"),
                    tx("tx-005", "DEBITO", "TRANSFERENCIA_MESMA_INSTITUICAO", "800.00", "2026-09-15", null,
                            "Conta poupança")),
            "acc-002", List.of(
                    tx("tx-101", "CREDITO", "TRANSFERENCIA_MESMA_INSTITUICAO", "800.00", "2026-09-15", null,
                            "Conta corrente"),
                    tx("tx-102", "CREDITO", "OUTROS", "21.47", "2026-09-30", null, "Rendimento poupança")));

    static final List<CardAccount> CARD_ACCOUNTS = List.of(
            new CardAccount("card-001", "MASTERCARD", "Mock Platinum", "CARTAO_CREDITO",
                    money("3200.00"), money("5000.00")));

    static final Map<String, List<CardBill>> CARD_BILLS = Map.of(
            "card-001", List.of(
                    new CardBill("bill-2026-08", LocalDate.parse("2026-08-10"), LocalDate.parse("2026-08-03"),
                            money("1234.56"), "BRL", false),
                    new CardBill("bill-2026-09", LocalDate.parse("2026-09-10"), LocalDate.parse("2026-09-03"),
                            money("1800.00"), "BRL", false)));

    static final Map<String, List<CardTransaction>> CARD_TRANSACTIONS = Map.of(
            "card-001", List.of(
                    new CardTransaction("ctx-001", false, 0, 0, "SUPERMERCADO BOM PRECO", money("412.80"),
                            LocalDate.parse("2026-07-20"), "bill-2026-08"),
                    new CardTransaction("ctx-002", true, 2, 10, "LOJA ELETRO NOTEBOOK", money("350.00"),
                            LocalDate.parse("2026-07-25"), "bill-2026-08"),
                    new CardTransaction("ctx-003", false, 0, 0, "STREAMING MUSICA", money("21.90"),
                            LocalDate.parse("2026-08-15"), "bill-2026-09"),
                    new CardTransaction("ctx-004", true, 3, 10, "LOJA ELETRO NOTEBOOK", money("350.00"),
                            LocalDate.parse("2026-08-25"), "bill-2026-09")));

    static final List<BankFixedIncome> BANK_FIXED_INCOMES = List.of(
            new BankFixedIncome("inv-cdb-001", "CDB LIQUIDEZ DIARIA", "Mock Bank S.A.", money("1.10"), "CDI",
                    LocalDate.parse("2028-01-15"), money("10000.00"), money("11342.18")));

    static final List<Fund> FUNDS = List.of(
            new Fund("inv-fund-001", "MOCK FIC FIM CREDITO PRIVADO", "12.345.678/0001-90", money("1532.441"),
                    money("3.4120"), LocalDateTime.parse("2026-09-29T18:00:00"), money("5228.69")));

    static final List<VariableIncome> VARIABLE_INCOMES = List.of(
            new VariableIncome("inv-var-001", "PETR4", "ACAO", money("100"), money("38.72"),
                    LocalDate.parse("2026-09-29"), money("3872.00")),
            new VariableIncome("inv-var-002", "IVVB11", "ETF", money("15"), money("352.10"),
                    LocalDate.parse("2026-09-29"), money("5281.50")));

    private static AccountTransaction tx(String id, String creditDebit, String type, String amount, String date,
            String counterpartyBrand, String counterpartyName) {
        return new AccountTransaction(id, "TRANSACAO_EFETIVADA", creditDebit, type, money(amount), "BRL",
                LocalDate.parse(date), counterpartyBrand, counterpartyName);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
