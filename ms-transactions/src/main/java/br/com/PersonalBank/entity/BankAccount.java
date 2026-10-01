package br.com.PersonalBank.entity;

import br.com.PersonalBank.dto.account.Account;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "bank_accounts", uniqueConstraints = @UniqueConstraint(columnNames = { "consent_id", "account_id" }))
public class BankAccount extends PanacheEntity {

    @Column(name = "consent_id", nullable = false)
    public String consentId;

    @Column(name = "account_id", nullable = false)
    public String accountId;

    @Column(name = "brand_name")
    public String brandName;

    @Column(name = "type")
    public String type;

    @Column(name = "number")
    public String number;

    /** Cria ou atualiza a conta, para que reprocessar um evento não duplique dados. */
    public static void upsert(String consentId, Account dto) {
        BankAccount entity = BankAccount.<BankAccount>find("consentId = ?1 and accountId = ?2", consentId, dto.accountId())
                .firstResultOptional()
                .orElseGet(BankAccount::new);
        entity.consentId = consentId;
        entity.accountId = dto.accountId();
        entity.brandName = dto.brandName();
        entity.type = dto.type();
        entity.number = dto.number();
        entity.persist();
    }
}
