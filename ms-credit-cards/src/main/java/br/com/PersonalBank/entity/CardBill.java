package br.com.PersonalBank.entity;

import br.com.PersonalBank.dto.card.CreditCardInvoice;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "card_bills", uniqueConstraints = @UniqueConstraint(columnNames = { "consent_id", "bill_id" }))
public class CardBill extends PanacheEntity {

    @Column(name = "consent_id", nullable = false)
    public String consentId;

    @Column(name = "account_id", nullable = false)
    public String accountId;

    @Column(name = "bill_id", nullable = false)
    public String billId;

    @Column(name = "due_date")
    public LocalDate dueDate;

    @Column(name = "closing_date")
    public LocalDate closingDate;

    @Column(name = "total_amount", precision = 19, scale = 2)
    public BigDecimal totalAmount;

    @Column(name = "currency")
    public String currency;

    /** Cria ou atualiza a fatura, para que reprocessar um evento não duplique dados. */
    public static void upsert(String consentId, String accountId, CreditCardInvoice dto) {
        CardBill entity = CardBill.<CardBill>find("consentId = ?1 and billId = ?2", consentId, dto.invoiceId())
                .firstResultOptional()
                .orElseGet(CardBill::new);
        entity.consentId = consentId;
        entity.accountId = accountId;
        entity.billId = dto.invoiceId();
        entity.dueDate = dto.dueDate();
        entity.closingDate = dto.closingDate();
        entity.totalAmount = dto.totalAmount();
        entity.currency = dto.totalAmountCurrency();
        entity.persist();
    }
}
