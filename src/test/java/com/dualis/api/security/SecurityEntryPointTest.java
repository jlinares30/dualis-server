package com.dualis.api.security;

import com.dualis.api.config.SecurityConfig;
import com.dualis.api.modules.account.application.usecase.ManageAccountUseCase;
import com.dualis.api.modules.account.controller.AccountController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the real security filter chain: requests without a valid token must get a JSON 401
 * (not 403), so the frontend can expire the session.
 */
@WebMvcTest(controllers = AccountController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class SecurityEntryPointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageAccountUseCase accountService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Missing token returns 401 JSON")
    void missingToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Invalid token returns 401 JSON")
    void invalidToken_Returns401() throws Exception {
        when(jwtTokenProvider.validateToken("forged")).thenReturn(false);

        mockMvc.perform(get("/api/v1/accounts/" + UUID.randomUUID())
                        .header("Authorization", "Bearer forged"))
                .andExpect(status().isUnauthorized());
    }
}
