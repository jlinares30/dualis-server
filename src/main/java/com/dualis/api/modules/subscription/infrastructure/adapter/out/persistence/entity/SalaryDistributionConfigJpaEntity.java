package com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "salary_distribution_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryDistributionConfigJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID workspaceId;

    @Column(length = 150)
    private String userEmail;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    @Builder.Default
    @Column(length = 20, nullable = false)
    private String frequency = "MONTHLY";

    @Builder.Default
    @Column(nullable = false)
    private Integer paymentDay = 30;

    @Builder.Default
    @Column(length = 20, nullable = false)
    private String distributionType = "SPLIT";

    private UUID primaryAccountId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean autoExecute = false;

    @Column(length = 20)
    private String lastExecutedDate;

    @Column(columnDefinition = "TEXT")
    private String branchesJson;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}