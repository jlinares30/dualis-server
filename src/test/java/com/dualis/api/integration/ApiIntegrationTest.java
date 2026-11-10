package com.dualis.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * End-to-end API flows against a real PostgreSQL, through the real security filter chain.
 * Runs only when DB_URL is set (see DualisApiApplicationTests).
 */
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
@SpringBootTest(properties = {
        "app.jwt.secret=test-only-secret-for-integration-tests-0123456789",
        "app.exchange-rates.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    private String run;

    @BeforeEach
    void setUp() {
        run = UUID.randomUUID().toString().substring(0, 8);
    }

    // ---------- helpers ----------

    private JsonNode call(MockHttpServletRequestBuilder req, String token, Object body, int expectedStatus) throws Exception {
        if (token != null) req.header("Authorization", "Bearer " + token);
        if (body != null) req.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        MvcResult res = mvc.perform(req).andReturn();
        assertThat(res.getResponse().getStatus())
                .as("%s %s -> %s", res.getRequest().getMethod(), res.getRequest().getRequestURI(), res.getResponse().getContentAsString())
                .isEqualTo(expectedStatus);
        String content = res.getResponse().getContentAsString();
        return content.isBlank() ? json.nullNode() : json.readTree(content);
    }

    private String register(String name) throws Exception {
        String email = name + "-" + run + "@example.com";
        JsonNode auth = call(post("/api/v1/auth/register"), null, java.util.Map.of(
                "email", email, "password", "Secret123!", "firstName", name, "lastName", "Test"), 201);
        return auth.get("accessToken").asText();
    }

    private String email(String name) {
        return name + "-" + run + "@example.com";
    }

    private String createWorkspace(String token, String type) throws Exception {
        return call(post("/api/v1/workspaces"), token, java.util.Map.of("name", type, "type", type, "currency", "PEN"), 201)
                .get("id").asText();
    }

    private String createAccount(String token, String workspaceId, String currency, String balance) throws Exception {
        return call(post("/api/v1/accounts"), token, java.util.Map.of("workspaceId", workspaceId, "name", "Cuenta " + currency,
                "type", "BANK", "balance", new BigDecimal(balance), "currency", currency), 201).get("id").asText();
    }

    private BigDecimal balance(String token, String accountId) throws Exception {
        return call(get("/api/v1/accounts/" + accountId), token, null, 200).get("balance").decimalValue();
    }

    // ---------- flows ----------

    @Test
    @DisplayName("Security: no token -> 401, other user's workspace -> 403, own data -> 200")
    void securityBoundaries() throws Exception {
        String jorge = register("jorge");
        String eve = register("eve");
        String ws = createWorkspace(jorge, "INDIVIDUAL");
        String acc = createAccount(jorge, ws, "PEN", "100");

        call(get("/api/v1/accounts/" + acc), null, null, 401);
        call(get("/api/v1/accounts/" + acc), eve, null, 403);
        call(get("/api/v1/accounts?workspaceId=" + ws), eve, null, 403);
        call(get("/api/v1/dashboard/summary?workspaceId=" + ws), eve, null, 403);
        assertThat(call(get("/api/v1/workspaces?userEmail=" + email("jorge")), eve, null, 200)).isEmpty();
        call(get("/api/v1/accounts/" + acc), jorge, null, 200);
    }

    @Test
    @DisplayName("Onboarding in couple mode never adds the partner without their consent")
    void onboardingDoesNotAddPartner() throws Exception {
        String jorge = register("jorge");
        String maria = register("maria");
        call(post("/api/v1/auth/onboarding"), jorge, java.util.Map.of("baseCurrency", "PEN", "accountName", "Principal",
                "accountType", "SAVINGS", "initialBalance", 0, "workspaceMode", "COUPLE", "partnerEmail", email("maria")), 200);

        JsonNode couple = null;
        for (JsonNode w : call(get("/api/v1/workspaces"), jorge, null, 200)) {
            if ("COUPLE".equals(w.get("type").asText())) couple = w;
        }
        assertThat(couple).isNotNull();
        assertThat(couple.get("members")).hasSize(1);
        assertThat(call(get("/api/v1/workspaces"), maria, null, 200)).isEmpty();

        // Maria joins only by using the invitation code
        call(post("/api/v1/workspaces/join"), maria, java.util.Map.of("invitationCode", couple.get("invitationCode").asText()), 200);
        assertThat(call(get("/api/v1/workspaces/" + couple.get("id").asText()), jorge, null, 200).get("members")).hasSize(2);
    }

    @Test
    @DisplayName("Couple balance uses each expense's split and settlement direction")
    void coupleBalance() throws Exception {
        String jorge = register("jorge");
        String maria = register("maria");
        JsonNode couple = call(post("/api/v1/workspaces"), jorge, java.util.Map.of("name", "Pareja", "type", "COUPLE", "currency", "PEN"), 201);
        String cid = couple.get("id").asText();
        call(post("/api/v1/workspaces/join"), maria, java.util.Map.of("invitationCode", couple.get("invitationCode").asText()), 200);
        String mariaAcc = createAccount(maria, createWorkspace(maria, "INDIVIDUAL"), "PEN", "1000");

        call(post("/api/v1/transactions"), maria, java.util.Map.of("workspaceId", cid, "accountId", mariaAcc, "type", "EXPENSE",
                "amount", 200, "description", "Cena", "splitPartnerAPercentage", 70), 201);
        JsonNode settlement = call(post("/api/v1/settlements"), jorge, java.util.Map.of("workspaceId", cid,
                "payerEmail", email("jorge"), "recipientEmail", email("maria"), "amount", 40, "currency", "PEN"), 201);
        call(patch("/api/v1/settlements/" + settlement.get("id").asText() + "/complete"), jorge, null, 200);

        JsonNode summary = call(get("/api/v1/settlements/balance-summary?workspaceId=" + cid), maria, null, 200);
        assertThat(summary.get("debtorEmail").asText()).isEqualTo(email("jorge"));
        assertThat(summary.get("netBalance").decimalValue()).isEqualByComparingTo("100.00"); // 140 share - 40 paid
        assertThat(summary.get("currency").asText()).isEqualTo("PEN");
    }

    @Test
    @DisplayName("Monthly bill payments: pay per month, no double payment, undo reverts the expense")
    void monthlyBills() throws Exception {
        String jorge = register("jorge");
        String ws = createWorkspace(jorge, "INDIVIDUAL");
        String acc = createAccount(jorge, ws, "PEN", "500");
        String sub = call(post("/api/v1/subscriptions"), jorge, java.util.Map.of("workspaceId", ws, "name", "Internet",
                "amount", 99.9, "dueDay", 10, "currency", "PEN", "category", "UTILITIES"), 201).get("id").asText();

        call(post("/api/v1/subscriptions/" + sub + "/payments"), jorge, java.util.Map.of("periodYear", 2026, "periodMonth", 10,
                "amount", 105, "accountId", acc), 201);
        call(post("/api/v1/subscriptions/" + sub + "/payments"), jorge, java.util.Map.of("periodYear", 2026, "periodMonth", 10), 400);
        assertThat(balance(jorge, acc)).isEqualByComparingTo("395.00");

        assertThat(call(get("/api/v1/subscriptions?workspaceId=" + ws + "&year=2026&month=10"), jorge, null, 200)
                .get(0).get("isPaidThisMonth").asBoolean()).isTrue();
        assertThat(call(get("/api/v1/subscriptions?workspaceId=" + ws + "&year=2026&month=11"), jorge, null, 200)
                .get(0).get("isPaidThisMonth").asBoolean()).isFalse();

        call(delete("/api/v1/subscriptions/" + sub + "/payments?year=2026&month=10"), jorge, null, 200);
        assertThat(balance(jorge, acc)).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Cross-currency transfer keeps the user's rate and reverts exactly")
    void crossCurrencyTransfer() throws Exception {
        String jorge = register("jorge");
        String ws = createWorkspace(jorge, "INDIVIDUAL");
        String usd = createAccount(jorge, ws, "USD", "500");
        String pen = createAccount(jorge, ws, "PEN", "0");

        String tx = call(post("/api/v1/transactions"), jorge, java.util.Map.of("workspaceId", ws, "accountId", usd,
                "targetAccountId", pen, "type", "TRANSFER", "amount", 100, "exchangeRate", 3.70, "description", "Cambio"), 201)
                .get("id").asText();
        assertThat(balance(jorge, pen)).isEqualByComparingTo("370.00");

        call(delete("/api/v1/transactions/" + tx), jorge, null, 204);
        assertThat(balance(jorge, usd)).isEqualByComparingTo("500.00");
        assertThat(balance(jorge, pen)).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("The offline sync API is disabled by default")
    void syncDisabledByDefault() throws Exception {
        String jorge = register("jorge");
        String ws = createWorkspace(jorge, "INDIVIDUAL");
        call(post("/api/v1/sync/pull"), jorge, java.util.Map.of("workspaceId", ws), 404);
        call(post("/api/v1/sync/push"), jorge, java.util.Map.of("workspaceId", ws), 404);
    }
}
