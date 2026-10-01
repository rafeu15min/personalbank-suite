package br.com.PersonalBank.entity;

import br.com.PersonalBank.dto.transaction.Transaction;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "account_transactions", uniqueConstraints = @UniqueConstraint(columnNames = { "consent_id", "transaction_id" }))
public class AccountTransaction extends PanacheEntity {

    @Column(name = "consent_id", nullable = false)
    public String consentId;

    @Column(name = "account_id", nullable = false)
    public String accountId;

    @Column(name = "transaction_id", nullable = false)
    public String transactionId;

    @Column(name = "credit_debit_type")
    public String creditDebitType; // "CREDITO" ou "DEBITO"

    @Column(name = "type")
    public String type; // Ex: "PIX", "TED", "BOLETO"

    @Column(name = "amount", precision = 19, scale = 2)
    public BigDecimal amount;

    @Column(name = "currency")
    public String currency;

    @Column(name = "transaction_date")
    public LocalDate transactionDate;

    @Column(name = "counterparty_name")
    public String counterpartyName;

    /** Cria ou atualiza a transação, para que reprocessar um evento não duplique dados. */
    public static void upsert(String consentId, String accountId, Transaction dto) {
        AccountTransaction entity = AccountTransaction.<AccountTransaction>find(
                "consentId = ?1 and transactionId = ?2", consentId, dto.transactionId())
                .firstResultOptional()
                .orElseGet(AccountTransaction::new);
        entity.consentId = consentId;
        entity.accountId = accountId;
        entity.transactionId = dto.transactionId();
        entity.creditDebitType = dto.creditDebitType();
        entity.type = dto.type();
        entity.amount = dto.amount();
        entity.currency = dto.transactionCurrency();
        entity.transactionDate = dto.transactionDate();
        entity.counterpartyName = dto.partieCounterpartyName();
        entity.persist();
    }
}
