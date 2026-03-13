package io.github.tare99.paymentprocessor.api.controller;

import io.github.tare99.paymentprocessor.domain.service.ApiKeyService;
import io.github.tare99.paymentprocessor.domain.service.ApiKeyService.GeneratedKey;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final ApiKeyService apiKeyService;

  public AuthController(ApiKeyService apiKeyService) {
    this.apiKeyService = apiKeyService;
  }

  @PostMapping("/api-keys")
  public ResponseEntity<ApiKeyResponse> createApiKey(
      @Valid @RequestBody CreateApiKeyRequest request) {
    GeneratedKey key = apiKeyService.generate(request.name(), request.accountNumber());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiKeyResponse(key.apiKey(), key.name(), key.accountNumber()));
  }

  public record CreateApiKeyRequest(@NotBlank String name, @NotBlank String accountNumber) {}

  public record ApiKeyResponse(String apiKey, String name, String accountNumber) {}
}
