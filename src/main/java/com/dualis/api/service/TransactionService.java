package com.dualis.api.service;

import com.dualis.api.dto.request.CreateTransactionRequest;
import com.dualis.api.dto.response.TransactionResponse;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(CreateTransactionRequest request);

    List<TransactionResponse> getTransactionsByWorkspace(UUID workspaceId);

    TransactionResponse getTransactionById(UUID id);
}
