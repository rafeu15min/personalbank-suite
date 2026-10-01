package br.com.PersonalBank.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

/**
 * Uma posição de investimento, de qualquer tipo de produto.
 */
@Entity
@Table(name = "investment_holdings", uniqueConstraints = @UniqueConstraint(columnNames = { "consent_id", "investment_id" }))
public class InvestmentHolding extends PanacheEntity {

    @Column(name = "consent_id", nullable = false)
    public String consentId;

    @Column(name = "investment_id", nullable = false)
    public String investmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    public InvestmentType type;

    @Column(name = "name")
    public String name; // Nome do produto, do fundo ou ticker do ativo

    @Column(name = "quantity", precision = 19, scale = 6)
    public BigDecimal quantity; // Cotas ou ações; nulo para renda fixa

    @Column(name = "current_amount", precision = 19, scale = 2)
    public BigDecimal currentAmount; // Valor atual da posição

    /** Cria ou atualiza a posição, para que reprocessar um evento não duplique dados. */
    public static void upsert(String consentId, String investmentId, InvestmentType type, String name,
            BigDecimal quantity, BigDecimal currentAmount) {
        InvestmentHolding entity = InvestmentHolding.<InvestmentHolding>find(
                "consentId = ?1 and investmentId = ?2", consentId, investmentId)
                .firstResultOptional()
                .orElseGet(InvestmentHolding::new);
        entity.consentId = consentId;
        entity.investmentId = investmentId;
        entity.type = type;
        entity.name = name;
        entity.quantity = quantity;
        entity.currentAmount = currentAmount;
        entity.persist();
    }
}
