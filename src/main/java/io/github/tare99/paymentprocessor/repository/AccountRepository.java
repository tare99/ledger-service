package io.github.tare99.paymentprocessor.repository;

import io.github.tare99.paymentprocessor.entity.Account;
import io.github.tare99.paymentprocessor.entity.AccountStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
  Optional<Account> findByAccountNumberAndStatus(String accountNumber, AccountStatus status);
}
