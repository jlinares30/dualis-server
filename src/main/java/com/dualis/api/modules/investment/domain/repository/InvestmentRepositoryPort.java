package com.dualis.api.modules.investment.domain.repository;

import com.dualis.api.modules.investment.domain.model.Investment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentRepositoryPort {
    Investment save(Investment investment);
    Optional<Investment> findById(UUID id);
    List<Investment> findByWorkspaceId(UUID workspaceId);
    void delete(UUID id);
    boolean existsById(UUID id);
}
