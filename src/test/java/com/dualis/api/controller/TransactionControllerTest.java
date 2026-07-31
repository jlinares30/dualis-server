package com.dualis.api.controller;

import com.dualis.api.domain.model.TransactionType;
import com.dualis.api.dto.request.CreateTransactionRequest;
import com.dualis.api.dto.request.UpdateTransactionRequest;
import com.dualis.api.dto.response.TransactionResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    private UUID workspaceId;
    private UUID transactionId;
    private UUID accountId;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        transactionResponse = TransactionResponse.builder()
                .id(transactionId)
                .workspaceId(workspaceId)
                .accountId(accountId)
                .accountName("Main Account")
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("45.50"))
                .currency("USD")
                .description("Grocery store")
                .transactionDate(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/transactions - Success")
    void createTransaction_Success() throws Exception {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .workspaceId(workspaceId)
                .accountId(accountId)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("45.50"))
                .currency("USD")
                .description("Grocery store")
                .build();

        when(transactionService.createTransaction(any(CreateTransactionRequest.class)))
                .thenReturn(transactionResponse);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.amount").value(45.50))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    @DisplayName("POST /api/v1/transactions - Validation Error when amount is negative")
    void createTransaction_NegativeAmount_ValidationError() throws Exception {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .workspaceId(workspaceId)
                .accountId(accountId)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("-10.00"))
                .currency("USD")
                .build();

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/transactions - Paginated Success")
    void getTransactions_Success() throws Exception {
        PageImpl<TransactionResponse> page = new PageImpl<>(List.of(transactionResponse));

        when(transactionService.getTransactions(
                eq(workspaceId), any(), any(), any(), any(), any(), any(), any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("workspaceId", workspaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(transactionId.toString()))
                .andExpect(jsonPath("$.content[0].type").value("EXPENSE"));
    }

    @Test
    @DisplayName("GET /api/v1/transactions/{id} - Success")
    void getTransactionById_Success() throws Exception {
        when(transactionService.getTransactionById(transactionId)).thenReturn(transactionResponse);

        mockMvc.perform(get("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/transactions/{id} - Not Found")
    void getTransactionById_NotFound() throws Exception {
        when(transactionService.getTransactionById(transactionId))
                .thenThrow(new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        mockMvc.perform(get("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/v1/transactions/{id} - Success")
    void updateTransaction_Success() throws Exception {
        UpdateTransactionRequest request = UpdateTransactionRequest.builder()
                .accountId(accountId)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .description("Updated grocery store")
                .build();

        when(transactionService.updateTransaction(eq(transactionId), any(UpdateTransactionRequest.class)))
                .thenReturn(transactionResponse);

        mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/transactions/{id} - Success (204 No Content)")
    void deleteTransaction_Success() throws Exception {
        doNothing().when(transactionService).deleteTransaction(transactionId);

        mockMvc.perform(delete("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isNoContent());
    }
}
