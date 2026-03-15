package io.github.tare99.paymentprocessor.domain.repository;

import io.github.tare99.paymentprocessor.domain.entity.LedgerEntry;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

  @Query(
      "SELECT le FROM LedgerEntry le JOIN FETCH le.account"
          + " WHERE le.transaction.id = :transactionId ORDER BY le.id ASC")
  List<LedgerEntry> findEntriesWithAccount(@Param("transactionId") Long transactionId);

  @Query(
      "SELECT le FROM LedgerEntry le JOIN FETCH le.account"
          + " WHERE le.transaction.id IN :transactionIds ORDER BY le.id ASC")
  List<LedgerEntry> findEntriesWithAccountByTransactionIds(
      @Param("transactionIds") List<Long> transactionIds);

  @Query(
      value =
          "SELECT le FROM LedgerEntry le JOIN FETCH le.transaction"
              + " WHERE le.account.accountNumber = :accountNumber",
      countQuery =
          "SELECT count(le) FROM LedgerEntry le"
              + " WHERE le.account.accountNumber = :accountNumber")
  Page<LedgerEntry> findEntriesByAccountNumber(
      @Param("accountNumber") String accountNumber, Pageable pageable);

  List<LedgerEntry> findByTransactionTransactionIdOrderByIdAsc(String transactionId);
}