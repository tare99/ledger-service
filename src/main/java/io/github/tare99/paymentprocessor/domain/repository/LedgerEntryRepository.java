package io.github.tare99.paymentprocessor.domain.repository;

import io.github.tare99.paymentprocessor.domain.entity.LedgerEntry;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

  List<LedgerEntry> findByPaymentPaymentIdOrderByIdAsc(String paymentId);

  @Query(
      "SELECT le FROM LedgerEntry le JOIN FETCH le.account"
          + " WHERE le.payment.id = :paymentId ORDER BY le.id ASC")
  List<LedgerEntry> findEntriesWithAccount(@Param("paymentId") Long paymentId);

  @Query(
      "SELECT le FROM LedgerEntry le JOIN FETCH le.account"
          + " WHERE le.payment.id IN :paymentIds ORDER BY le.id ASC")
  List<LedgerEntry> findEntriesWithAccountByPaymentIds(
      @Param("paymentIds") List<Long> paymentIds);

  @Query(
      """
      SELECT le.balanceAfter FROM LedgerEntry le
      WHERE le.account.id = :accountId
      ORDER BY le.id DESC
      LIMIT 1
      """)
  BigDecimal findLatestBalanceAfter(@Param("accountId") Long accountId);
}
