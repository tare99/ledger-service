package io.github.tare99.paymentprocessor.domain.repository;

import io.github.tare99.paymentprocessor.domain.entity.ApiKey;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

  @Query(
      "SELECT a FROM ApiKey a JOIN FETCH a.account WHERE a.keyHash = :keyHash AND a.active = true")
  Optional<ApiKey> findByKeyHashAndActiveTrueWithAccount(String keyHash);
}
