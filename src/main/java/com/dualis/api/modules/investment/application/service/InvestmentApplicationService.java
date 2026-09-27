package com.dualis.api.modules.investment.application.service;

import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.modules.investment.application.usecase.ManageInvestmentUseCase;
import com.dualis.api.modules.investment.domain.model.Investment;
import com.dualis.api.modules.investment.domain.repository.InvestmentRepositoryPort;
import com.dualis.api.modules.investment.dto.request.CreateInvestmentRequest;
import com.dualis.api.modules.investment.dto.request.UpdateInvestmentRequest;
import com.dualis.api.modules.investment.dto.response.InvestmentResponse;
import com.dualis.api.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvestmentApplicationService implements ManageInvestmentUseCase {

    private final InvestmentRepositoryPort repository;

    @Override
    @Transactional
    public InvestmentResponse createInvestment(CreateInvestmentRequest request) {
        String currency = request.getCurrency() != null && !request.getCurrency().isBlank()
                ? request.getCurrency().trim().toUpperCase()
                : "PEN";
        BigDecimal initialCapital = request.getInitialCapital() != null ? request.getInitialCapital() : BigDecimal.ZERO;
        BigDecimal currentValue = request.getCurrentValue() != null ? request.getCurrentValue() : initialCapital;

        Investment investment = Investment.builder()
                .workspaceId(request.getWorkspaceId())
                .name(request.getName().trim())
                .institution(request.getInstitution().trim())
                .type(request.getType().trim())
                .initialCapital(Money.of(initialCapital, currency))
                .currentValue(Money.of(currentValue, currency))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        Investment saved = repository.save(investment);
        return InvestmentResponse.fromDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentResponse> getInvestmentsByWorkspace(UUID workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
                .map(InvestmentResponse::fromDomain)
                .toList();
    }

    @Override
    @Transactional
    public InvestmentResponse updateInvestment(UUID id, UpdateInvestmentRequest request) {
        Investment investment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found with id: " + id));

        investment.updateDetails(
                request.getName(),
                request.getInstitution(),
                request.getType(),
                request.getInitialCapital(),
                request.getCurrentValue()
        );

        Investment saved = repository.save(investment);
        return InvestmentResponse.fromDomain(saved);
    }

    @Override
    @Transactional
    public void deleteInvestment(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Investment not found with id: " + id);
        }
        repository.delete(id);
    }
}
