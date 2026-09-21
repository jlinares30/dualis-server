package com.dualis.api.modules.account.controller;

import com.dualis.api.modules.account.application.usecase.ManageAccountUseCase;
import com.dualis.api.modules.account.domain.model.AccountStatus;
import com.dualis.api.modules.account.domain.model.AccountType;
import com.dualis.api.modules.account.dto.request.CreateAccountRequest;
import com.dualis.api.modules.account.dto.request.UpdateAccountRequest;
import com.dualis.api.modules.account.dto.response.AccountResponse;
import com.dualis.api.exception.ResourceNotFoundException;
import com.dualis.api.security.JwtTokenProvider;
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

@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManageAccountUseCase accountService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

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
                .name("Savings Account")
                .type(AccountType.BANK)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .description("Personal savings")
                .isIncludedInTotal(true)
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/accounts - Should return 201 when request is valid")
    void createAccount_WhenValidRequest_ShouldReturn201() throws Exception {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .workspaceId(workspaceId)
                .name("Savings Account")
                .type(AccountType.BANK)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .description("Personal savings")
                .isIncludedInTotal(true)
                .build();

        when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(accountResponse);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.name").value("Savings Account"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/accounts - Should return 400 when name is blank")
    void createAccount_WhenNameIsBlank_ShouldReturn400() throws Exception {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .workspaceId(workspaceId)
                .name("")
                .type(AccountType.BANK)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .build();

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/accounts - Should return list of accounts for workspace")
    void getAccountsByWorkspace_ShouldReturnAccountsList() throws Exception {
        when(accountService.getAccountsByWorkspace(eq(workspaceId), eq(null)))
                .thenReturn(List.of(accountResponse));

        mockMvc.perform(get("/api/v1/accounts")
                        .param("workspaceId", workspaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(accountId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} - Should return 200 when found")
    void getAccountById_WhenExists_ShouldReturn200() throws Exception {
        when(accountService.getAccountById(accountId)).thenReturn(accountResponse);

        mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.name").value("Savings Account"));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} - Should return 404 when not found")
    void getAccountById_WhenNotFound_ShouldReturn404() throws Exception {
        when(accountService.getAccountById(accountId))
                .thenThrow(new ResourceNotFoundException("Account not found with id: " + accountId));

        mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/accounts/{id} - Should update and return 200")
    void updateAccount_WhenValid_ShouldReturn200() throws Exception {
        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .name("Updated Savings Account")
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountService.updateAccount(eq(accountId), any(UpdateAccountRequest.class)))
                .thenReturn(accountResponse);

        mockMvc.perform(put("/api/v1/accounts/{id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()));
    }

    @Test
    @DisplayName("DELETE /api/v1/accounts/{id} - Should archive and return 204")
    void archiveAccount_ShouldReturn204() throws Exception {
        doNothing().when(accountService).archiveAccount(accountId);

        mockMvc.perform(delete("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isNoContent());
    }
}
