package io.github.tare99.paymentprocessor.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.tare99.paymentprocessor.api.controller.AuthController;
import io.github.tare99.paymentprocessor.api.controller.AuthController.CreateApiKeyRequest;
import io.github.tare99.paymentprocessor.domain.exception.AccountNotFoundException;
import io.github.tare99.paymentprocessor.domain.service.ApiKeyService;
import io.github.tare99.paymentprocessor.domain.service.ApiKeyService.GeneratedKey;
import io.github.tare99.paymentprocessor.security.ApiKeyAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private ApiKeyService apiKeyService;
  @MockitoBean private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

  @Test
  void createApiKeyReturns201() throws Exception {
    var request = new CreateApiKeyRequest("my-key", "ACC-ALICE00000000001");
    var generatedKey =
        new GeneratedKey(
            "pp_live_abcdefgh12345678901234567890123456789012",
            "my-key",
            "ACC-ALICE00000000001");

    when(apiKeyService.generate(eq("my-key"), eq("ACC-ALICE00000000001"))).thenReturn(generatedKey);

    mockMvc
        .perform(
            post("/api/v1/auth/api-keys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.apiKey").value(generatedKey.apiKey()))
        .andExpect(jsonPath("$.name").value("my-key"))
        .andExpect(jsonPath("$.accountNumber").value("ACC-ALICE00000000001"));
  }

  @Test
  void createApiKeyWithMissingNameReturns400() throws Exception {
    String body =
        """
        {"accountNumber": "ACC-ALICE00000000001"}
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/api-keys").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createApiKeyWithMissingAccountNumberReturns400() throws Exception {
    String body =
        """
        {"name": "my-key"}
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/api-keys").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createApiKeyWithBlankFieldsReturns400() throws Exception {
    String body =
        """
        {"name": "", "accountNumber": ""}
        """;

    mockMvc
        .perform(
            post("/api/v1/auth/api-keys").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createApiKeyWithNonExistentAccountReturns404() throws Exception {
    var request = new CreateApiKeyRequest("my-key", "ACC-NONEXISTENT00001");

    when(apiKeyService.generate(eq("my-key"), eq("ACC-NONEXISTENT00001")))
        .thenThrow(new AccountNotFoundException("Account not found: ACC-NONEXISTENT00001"));

    mockMvc
        .perform(
            post("/api/v1/auth/api-keys")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Account Not Found"));
  }
}
