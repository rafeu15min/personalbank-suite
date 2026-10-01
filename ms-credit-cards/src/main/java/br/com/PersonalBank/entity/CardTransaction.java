package br.com.PersonalBank.entity;

import br.com.PersonalBank.dto.card.CreditCardTransaction;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "card_transactions", uniqueConstraints = @UniqueConstraint(columnNames = { "consent_id", "transaction_id" }))
public class CardTransaction extends PanacheEntity {

    @Column(name = "consent_id", nullable = false)
    public String consentId;

    @Column(name = "account_id", nullable = false)
    public String accountId;

    @Column(name = "transaction_id", nullable = false)
    public String transactionId;

    @Column(name = "bill_id")
    public String billId;

    @Column(name = "description")
    public String description;

    @Column(name = "amount", precision = 19, scale = 2)
    public BigDecimal amount;

    @Column(name = "transaction_date")
    public LocalDate transactionDate;

    @Column(name = "instalment_number")
    public Integer instalmentNumber; // Nulo quando a compra não é parcelada

    @Column(name = "total_instalments")
    public Integer totalInstalments;

    /** Cria ou atualiza o lançamento, para que reprocessar um evento não duplique dados. */
    public static void upsert(String consentId, String accountId, CreditCardTransaction dto) {
        CardTransaction entity = CardTransaction.<CardTransaction>find(
                "consentId = ?1 and transactionId = ?2", consentId, dto.transactionId())
                .firstResultOptional()
                .orElseGet(CardTransaction::new);
        entity.consentId = consentId;
        entity.accountId = accountId;
        entity.transactionId = dto.transactionId();
        entity.billId = dto.billId();
        entity.description = dto.description();
        entity.amount = dto.amount();
        entity.transactionDate = dto.transactionDate();
        entity.instalmentNumber = dto.isInstalment() ? dto.instalmentNumber() : null;
        entity.totalInstalments = dto.isInstalment() ? dto.totalInstalments() : null;
        entity.persist();
    }
}
