package io.github.tare99.paymentprocessor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.tare99.paymentprocessor.domain.entity.ApiKey;
import io.github.tare99.paymentprocessor.domain.exception.AccountNotFoundException;
import io.github.tare99.paymentprocessor.domain.repository.ApiKeyRepository;
import io.github.tare99.paymentprocessor.domain.service.ApiKeyService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AuthIT extends BaseIT {

  @Autowired private ApiKeyService apiKeyService;
  @Autowired private ApiKeyRepository apiKeyRepository;

  @Test
  void generateAndValidateApiKey() {
    var generated = apiKeyService.generate("test-key", "ACC-ALICE00000000001");

    assertThat(generated.apiKey()).startsWith(ApiKeyService.KEY_PREFIX);
    assertThat(generated.apiKey())
        .hasSize(ApiKeyService.KEY_PREFIX.length() + ApiKeyService.API_KEY_LENGTH);
    assertThat(generated.name()).isEqualTo("test-key");
    assertThat(generated.accountNumber()).isEqualTo("ACC-ALICE00000000001");

    Optional<ApiKey> validated = apiKeyService.validate(generated.apiKey());
    assertThat(validated).isPresent();
    assertThat(validated.get().getAccount().getAccountNumber()).isEqualTo("ACC-ALICE00000000001");
    assertThat(validated.get().getLastUsedAt()).isNotNull();
  }

  @Test
  void validateWithInvalidKeyReturnsEmpty() {
    Optional<ApiKey> result =
        apiKeyService.validate("pp_live_invalidkey00000000000000000000000000");
    assertThat(result).isEmpty();
  }

  @Test
  void validateWithWrongPrefixReturnsEmpty() {
    Optional<ApiKey> result = apiKeyService.validate("wrong_prefix_key");
    assertThat(result).isEmpty();
  }

  @Test
  void validateWithTooShortKeyReturnsEmpty() {
    Optional<ApiKey> result = apiKeyService.validate("pp_live_short");
    assertThat(result).isEmpty();
  }

  @Test
  void generateForNonExistentAccountThrows() {
    assertThatThrownBy(() -> apiKeyService.generate("key", "ACC-NONEXISTENT00001"))
        .isInstanceOf(AccountNotFoundException.class);
  }

  @Test
  void generatedKeyIsStoredInDatabase() {
    var generated = apiKeyService.generate("db-check-key", "ACC-BOB000000000002");

    Optional<ApiKey> stored = apiKeyService.validate(generated.apiKey());
    assertThat(stored).isPresent();
    assertThat(stored.get().getName()).isEqualTo("db-check-key");
    assertThat(stored.get().isActive()).isTrue();
    assertThat(stored.get().getKeyHash()).isNotBlank();
  }
}
