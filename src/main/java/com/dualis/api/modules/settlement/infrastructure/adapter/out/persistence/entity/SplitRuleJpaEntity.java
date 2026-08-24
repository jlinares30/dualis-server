package com.dualis.api.modules.settlement.infrastructure.adapter.out.persistence.entity;

import com.dualis.api.modules.settlement.domain.model.SplitType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "split_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SplitRuleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", nullable = false, length = 30)
    private SplitType splitType;

    @Column(name = "partner_a_percentage", precision = 5, scale = 2)
    private BigDecimal partnerAPercentage;

    @Column(name = "partner_b_percentage", precision = 5, scale = 2)
    private BigDecimal partnerBPercentage;

    @Column(name = "partner_a_income", precision = 15, scale = 2)
    private BigDecimal partnerAIncome;

    @Column(name = "partner_b_income", precision = 15, scale = 2)
    private BigDecimal partnerBIncome;

    @Column(name = "partner_a_fixed_amount", precision = 15, scale = 2)
    private BigDecimal partnerAFixedAmount;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
        if (isDefault == null) {
            isDefault = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
