package io.github.tare99.paymentprocessor.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.tare99.paymentprocessor.api.controller.AuthController;
import io.github.tare99.paymentprocessor.api.controller.AuthController.CreateApiKeyRequest;
import io.github.tare99.paymentprocessor.domain.service.ApiKeyService;
import io.github.tare99.paymentprocessor.domain.service.ApiKeyService.GeneratedKey;
import io.github.tare99.paymentprocessor.security.ApiKeyAuthenticationFilter;
import io.github.tare99.paymentprocessor.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  private static final String ALICE = "ACC-ALICE00000000001";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private ApiKeyService apiKeyService;
  @MockitoBean private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

  @BeforeEach
  void setUpAuth() {
    authenticateAs(ALICE);
  }

  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createApiKeyReturns201() throws Exception {
    var request = new CreateApiKeyRequest("my-key");
    var generatedKey =
        new GeneratedKey(
            "pp_live_abcdefgh12345678901234567890123456789012", "my-key", ALICE);

    when(apiKeyService.generate(eq("my-key"), eq(ALICE))).thenReturn(generatedKey);

    mockMvc
        .perform(
            post("/api/v1/auth/api-keys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.apiKey").value(generatedKey.apiKey()))
        .andExpect(jsonPath("$.name").value("my-key"))
        .andExpect(jsonPath("$.accountNumber").value(ALICE));
  }

  @Test
  void createApiKeyWithMissingNameReturns400() throws Exception {
    String body = "{}";

    mockMvc
        .perform(
            post("/api/v1/auth/api-keys").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createApiKeyWithBlankNameReturns400() throws Exception {
    String body =
        """
        {"name": ""}
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/api-keys").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  private void authenticateAs(String accountNumber) {
    UserPrincipal principal = new UserPrincipal(accountNumber);
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    SecurityContextHolder.setContext(new SecurityContextImpl(auth));
  }
}