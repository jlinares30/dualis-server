package com.dualis.api.controller;

import com.dualis.api.domain.model.AccountStatus;
import com.dualis.api.domain.model.AccountType;
import com.dualis.api.dto.request.CreateAccountRequest;
import com.dualis.api.dto.request.UpdateAccountRequest;
import com.dualis.api.dto.response.AccountResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    private UUID workspaceId;
    private UUID accountId;
    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        accountResponse = AccountResponse.builder()
                .id(accountId)
                .workspaceId(workspaceId)
                .name("Main Checking")
                .type(AccountType.BANK)
                .balance(new BigDecimal("1500.00"))
                .currency("USD")
                .description("Daily expenses")
                .isIncludedInTotal(true)
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/accounts - Success")
    void createAccount_Success() throws Exception {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .workspaceId(workspaceId)
                .name("Main Checking")
                .type(AccountType.BANK)
                .balance(new BigDecimal("1500.00"))
                .currency("USD")
                .build();

        when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(accountResponse);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.name").value("Main Checking"))
                .andExpect(jsonPath("$.balance").value(1500.00));
    }

    @Test
    @DisplayName("POST /api/v1/accounts - Validation error when missing required fields")
    void createAccount_ValidationError() throws Exception {
        CreateAccountRequest invalidRequest = CreateAccountRequest.builder().build();

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/v1/accounts - Success")
    void getAccountsByWorkspace_Success() throws Exception {
        when(accountService.getAccountsByWorkspace(eq(workspaceId), any())).thenReturn(List.of(accountResponse));

        mockMvc.perform(get("/api/v1/accounts")
                        .param("workspaceId", workspaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(accountId.toString()))
                .andExpect(jsonPath("$[0].name").value("Main Checking"));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} - Success")
    void getAccountById_Success() throws Exception {
        when(accountService.getAccountById(accountId)).thenReturn(accountResponse);

        mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} - Not Found")
    void getAccountById_NotFound() throws Exception {
        when(accountService.getAccountById(accountId))
                .thenThrow(new ResourceNotFoundException("Account not found with id: " + accountId));

        mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/v1/accounts/{id} - Success")
    void updateAccount_Success() throws Exception {
        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .name("Updated Name")
                .build();

        when(accountService.updateAccount(eq(accountId), any(UpdateAccountRequest.class)))
                .thenReturn(accountResponse);

        mockMvc.perform(put("/api/v1/accounts/{id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{id} - Success (204 No Content)")
    void archiveAccount_Success() throws Exception {
        doNothing().when(accountService).archiveAccount(accountId);

        mockMvc.perform(delete("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isNoContent());
    }
}
