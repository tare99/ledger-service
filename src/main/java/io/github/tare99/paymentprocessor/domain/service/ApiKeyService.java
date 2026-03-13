package io.github.tare99.paymentprocessor.domain.service;

import io.github.tare99.paymentprocessor.domain.entity.Account;
import io.github.tare99.paymentprocessor.domain.entity.ApiKey;
import io.github.tare99.paymentprocessor.domain.exception.AccountNotFoundException;
import io.github.tare99.paymentprocessor.domain.repository.AccountRepository;
import io.github.tare99.paymentprocessor.domain.repository.ApiKeyRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiKeyService {

  public static final String KEY_PREFIX = "pp_live_";
  public static final int API_KEY_LENGTH = 40;
  private static final String ALPHABET =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private final ApiKeyRepository apiKeyRepository;
  private final AccountRepository accountRepository;

  public ApiKeyService(ApiKeyRepository apiKeyRepository, AccountRepository accountRepository) {
    this.apiKeyRepository = apiKeyRepository;
    this.accountRepository = accountRepository;
  }

  @Transactional
  public GeneratedKey generate(String name, String accountNumber) {
    Account account =
        accountRepository
            .findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

    String randomPart = randomAlphanumeric();
    String fullKey = KEY_PREFIX + randomPart;
    String keyHash = sha256Hex(randomPart);

    ApiKey apiKey = new ApiKey(keyHash, name, account);
    apiKeyRepository.save(apiKey);

    return new GeneratedKey(fullKey, name, account.getAccountNumber());
  }

  @Transactional
  public Optional<ApiKey> validate(String rawKey) {
    if (!rawKey.startsWith(KEY_PREFIX)) {
      return Optional.empty();
    }

    String randomPart = rawKey.substring(KEY_PREFIX.length());
    if (randomPart.isEmpty()) {
      return Optional.empty();
    }

    String incomingHash = sha256Hex(randomPart);
    Optional<ApiKey> apiKey = apiKeyRepository.findByKeyHashAndActiveTrueWithAccount(incomingHash);

    apiKey.ifPresent(key -> {
      key.recordUsage();
      apiKeyRepository.save(key);
    });

    return apiKey;
  }

  private static String randomAlphanumeric() {
    StringBuilder sb = new StringBuilder(API_KEY_LENGTH);
    for (int i = 0; i < API_KEY_LENGTH; i++) {
      sb.append(ALPHABET.charAt(SECURE_RANDOM.nextInt(ALPHABET.length())));
    }
    return sb.toString();
  }

  private static String sha256Hex(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  public record GeneratedKey(String apiKey, String name, String accountNumber) {}
}
