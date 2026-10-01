package br.com.PersonalBank.entity;

import br.com.PersonalBank.dto.card.CreditCardAccount;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(name = "card_accounts", uniqueConstraints = @UniqueConstraint(columnNames = { "consent_id", "account_id" }))
public class CardAccount extends PanacheEntity {

    @Column(name = "consent_id", nullable = false)
    public String consentId;

    @Column(name = "account_id", nullable = false)
    public String accountId;

    @Column(name = "brand_name")
    public String brandName;

    @Column(name = "name")
    public String name;

    @Column(name = "product_type")
    public String productType;

    @Column(name = "available_credit_limit", precision = 19, scale = 2)
    public BigDecimal availableCreditLimit;

    @Column(name = "total_credit_limit", precision = 19, scale = 2)
    public BigDecimal totalCreditLimit;

    /** Cria ou atualiza o cartão, para que reprocessar um evento não duplique dados. */
    public static void upsert(String consentId, CreditCardAccount dto) {
        CardAccount entity = CardAccount.<CardAccount>find(
                "consentId = ?1 and accountId = ?2", consentId, dto.creditCardAccountId())
                .firstResultOptional()
                .orElseGet(CardAccount::new);
        entity.consentId = consentId;
        entity.accountId = dto.creditCardAccountId();
        entity.brandName = dto.brandName();
        entity.name = dto.name();
        entity.productType = dto.productType();
        entity.availableCreditLimit = dto.availableCreditLimit();
        entity.totalCreditLimit = dto.totalCreditLimit();
        entity.persist();
    }
}
