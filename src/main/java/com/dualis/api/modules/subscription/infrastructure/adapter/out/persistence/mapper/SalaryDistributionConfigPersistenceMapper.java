package com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.mapper;

import com.dualis.api.modules.subscription.domain.model.SalaryDistributionBranch;
import com.dualis.api.modules.subscription.domain.model.SalaryDistributionConfig;
import com.dualis.api.modules.subscription.infrastructure.adapter.out.persistence.entity.SalaryDistributionConfigJpaEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SalaryDistributionConfigPersistenceMapper {

    private final ObjectMapper objectMapper;

    public SalaryDistributionConfigJpaEntity toEntity(SalaryDistributionConfig domain) {
        if (domain == null) return null;

        String branchesJson = null;
        if (domain.getBranches() != null) {
            try {
                branchesJson = objectMapper.writeValueAsString(domain.getBranches());
            } catch (Exception e) {
                log.error("Error serializing salary distribution branches to JSON", e);
            }
        }

        return SalaryDistributionConfigJpaEntity.builder()
                .id(domain.getId())
                .workspaceId(domain.getWorkspaceId())
                .userEmail(domain.getUserEmail())
                .enabled(domain.getEnabled())
                .frequency(domain.getFrequency())
                .paymentDay(domain.getPaymentDay())
                .distributionType(domain.getDistributionType())
                .primaryAccountId(domain.getPrimaryAccountId())
                .autoExecute(domain.getAutoExecute())
                .lastExecutedDate(domain.getLastExecutedDate())
                .branchesJson(branchesJson)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public SalaryDistributionConfig toDomain(SalaryDistributionConfigJpaEntity entity) {
        if (entity == null) return null;

        List<SalaryDistributionBranch> branches = new ArrayList<>();
        if (entity.getBranchesJson() != null && !entity.getBranchesJson().isBlank()) {
            try {
                branches = objectMapper.readValue(entity.getBranchesJson(), new TypeReference<List<SalaryDistributionBranch>>() {});
            } catch (Exception e) {
                log.error("Error deserializing salary distribution branches from JSON", e);
            }
        }

        return SalaryDistributionConfig.builder()
                .id(entity.getId())
                .workspaceId(entity.getWorkspaceId())
                .userEmail(entity.getUserEmail())
                .enabled(entity.getEnabled())
                .frequency(entity.getFrequency())
                .paymentDay(entity.getPaymentDay())
                .distributionType(entity.getDistributionType())
                .primaryAccountId(entity.getPrimaryAccountId())
                .autoExecute(entity.getAutoExecute())
                .lastExecutedDate(entity.getLastExecutedDate())
                .branches(branches)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}