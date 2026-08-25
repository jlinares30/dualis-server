package com.dualis.api.modules.account.application.usecase;

import com.dualis.api.domain.model.AccountStatus;
import com.dualis.api.dto.request.CreateAccountRequest;
import com.dualis.api.dto.request.UpdateAccountRequest;
import com.dualis.api.dto.response.AccountResponse;

import java.util.List;
import java.util.UUID;

public interface ManageAccountUseCase {
    AccountResponse createAccount(CreateAccountRequest request);
    AccountResponse updateAccount(UUID id, UpdateAccountRequest request);
    List<AccountResponse> getAccountsByWorkspace(UUID workspaceId, AccountStatus status);
    AccountResponse getAccountById(UUID id);
    void archiveAccount(UUID id);
}
