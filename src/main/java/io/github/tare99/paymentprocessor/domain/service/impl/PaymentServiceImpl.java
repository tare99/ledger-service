package io.github.tare99.paymentprocessor.domain.service.impl;

import io.github.tare99.paymentprocessor.api.request.CreatePaymentRequest;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import io.github.tare99.paymentprocessor.api.response.CreatePaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentStatusResponse;
import io.github.tare99.paymentprocessor.api.response.RefundPaymentResponse;
import io.github.tare99.paymentprocessor.domain.entity.Account;
import io.github.tare99.paymentprocessor.domain.entity.EntryType;
import io.github.tare99.paymentprocessor.domain.entity.LedgerEntry;
import io.github.tare99.paymentprocessor.domain.entity.Payment;
import io.github.tare99.paymentprocessor.domain.exception.PaymentNotFoundException;
import io.github.tare99.paymentprocessor.domain.mapper.PaymentMapper;
import io.github.tare99.paymentprocessor.domain.repository.LedgerEntryRepository;
import io.github.tare99.paymentprocessor.domain.repository.PaymentRepository;
import io.github.tare99.paymentprocessor.domain.service.AccountService;
import io.github.tare99.paymentprocessor.domain.service.PaymentService;
import io.github.tare99.paymentprocessor.domain.service.dto.SenderAndReceiver;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {

  private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

  private final PaymentRepository paymentRepository;
  private final AccountService accountService;
  private final PaymentMapper paymentMapper;
  private final LedgerEntryRepository ledgerEntryRepository;

  public PaymentServiceImpl(
      PaymentRepository paymentRepository,
      AccountService accountService,
      PaymentMapper paymentMapper,
      LedgerEntryRepository ledgerEntryRepository) {
    this.paymentRepository = paymentRepository;
    this.accountService = accountService;
    this.paymentMapper = paymentMapper;
    this.ledgerEntryRepository = ledgerEntryRepository;
  }

  @Override
  @Transactional
  public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
    if (request.senderAccountId().equals(request.receiverAccountId())) {
      throw new IllegalArgumentException("Sender and receiver accounts must be different");
    }

    Optional<Payment> optionalPayment = paymentRepository.findByIdempotencyKey(request.requestId());
    if (optionalPayment.isPresent()) {
      log.info("Payment with request id {} already exists, will return OK", request.requestId());
      Payment existing = optionalPayment.get();
      return paymentMapper.toCreatePaymentResponse(existing, request);
    }

    SenderAndReceiver senderAndReceiver =
        accountService.getSenderAndReceiverForUpdate(
            request.senderAccountId(), request.receiverAccountId());
    BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
    senderAndReceiver.applyPayment(request.amount());

    Account sender = senderAndReceiver.sender();
    Account receiver = senderAndReceiver.receiver();

    Payment payment = new Payment(PaymentStatus.COMPLETED, request.requestId());
    paymentRepository.insert(payment);
    List<LedgerEntry> entries = createLedgerEntries(payment, sender, receiver, amount);

    log.info(
        "Payment created: paymentId={} amount={} currency={}",
        payment.getPaymentId(),
        amount,
        request.currency());
    return paymentMapper.toCreatePaymentResponse(payment, entries);
  }

  @Override
  @Transactional
  public RefundPaymentResponse refundPayment(String paymentId) {
    Optional<Payment> optionalPayment =
        paymentRepository.findByPaymentIdAndStatusForUpdate(paymentId, PaymentStatus.COMPLETED);
    if (optionalPayment.isEmpty()) {
      throw new IllegalStateException("Payment not found or not in COMPLETED status");
    }
    Payment payment = optionalPayment.get();

    List<LedgerEntry> originalEntries =
        ledgerEntryRepository.findEntriesWithAccount(payment.getId());
    LedgerEntry originalDebit = findFirstByType(originalEntries, EntryType.DEBIT);
    LedgerEntry originalCredit = findFirstByType(originalEntries, EntryType.CREDIT);

    BigDecimal amount = originalDebit.getAmount();

    SenderAndReceiver senderAndReceiver =
        accountService.getSenderAndReceiverForUpdate(
            originalDebit.getAccount().getAccountNumber(),
            originalCredit.getAccount().getAccountNumber());
    senderAndReceiver.applyRefund(amount);
    Account sender = senderAndReceiver.sender();
    Account receiver = senderAndReceiver.receiver();

    payment.updateStatus(PaymentStatus.REFUNDED);
    createLedgerEntries(payment, receiver, sender, amount);

    log.info("Payment refunded: paymentId={}", paymentId);

    return new RefundPaymentResponse(
        paymentId, amount, sender.getCurrency(), PaymentStatus.REFUNDED);
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentResponse getPayment(String paymentId) {
    Payment payment = getPaymentById(paymentId);
    List<LedgerEntry> entries = ledgerEntryRepository.findEntriesWithAccount(payment.getId());
    return paymentMapper.toPaymentResponse(payment, entries);
  }

  @Override
  @Transactional(readOnly = true)
  public PaginatedPaymentResponse listPayments(
      String senderAccountNumber,
      String receiverAccountNumber,
      PaymentStatus status,
      int page,
      int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Specification<Payment> spec =
        buildSpecification(senderAccountNumber, receiverAccountNumber, status);
    Page<Payment> resultPage = paymentRepository.findAll(spec, pageable);

    List<Long> paymentIds = resultPage.getContent().stream().map(Payment::getId).toList();
    Map<Long, List<LedgerEntry>> entriesByPaymentId =
        paymentIds.isEmpty()
            ? Map.of()
            : ledgerEntryRepository.findEntriesWithAccountByPaymentIds(paymentIds).stream()
                .collect(Collectors.groupingBy(e -> e.getPayment().getId()));

    return paymentMapper.toPaginatedResponse(resultPage, entriesByPaymentId);
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentStatusResponse getPaymentStatus(String paymentId) {
    Payment payment = getPaymentById(paymentId);
    return new PaymentStatusResponse(paymentId, payment.getStatus());
  }

  private List<LedgerEntry> createLedgerEntries(
      Payment payment, Account debited, Account credited, BigDecimal amount) {
    LedgerEntry debit =
        new LedgerEntry(payment, debited, EntryType.DEBIT, amount, debited.getBalance());
    LedgerEntry credit =
        new LedgerEntry(payment, credited, EntryType.CREDIT, amount, credited.getBalance());
    ledgerEntryRepository.saveAll(List.of(debit, credit));
    return List.of(debit, credit);
  }

  private Payment getPaymentById(String paymentId) {
    return paymentRepository
        .findByPaymentId(paymentId)
        .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
  }

  private LedgerEntry findFirstByType(List<LedgerEntry> entries, EntryType type) {
    return entries.stream()
        .filter(e -> e.getEntryType() == type)
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException("No " + type + " ledger entry found for payment"));
  }

  private Specification<Payment> buildSpecification(
      String senderAccountNumber, String receiverAccountNumber, PaymentStatus status) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (senderAccountNumber != null && !senderAccountNumber.isBlank()) {
        var sub = query.subquery(Long.class);
        var le = sub.from(LedgerEntry.class);
        sub.select(le.get("id"))
            .where(
                builder.equal(le.get("payment").get("id"), root.get("id")),
                builder.equal(le.get("entryType"), EntryType.DEBIT),
                builder.equal(le.get("account").get("accountNumber"), senderAccountNumber));
        predicates.add(builder.exists(sub));
      }

      if (receiverAccountNumber != null && !receiverAccountNumber.isBlank()) {
        var sub = query.subquery(Long.class);
        var le = sub.from(LedgerEntry.class);
        sub.select(le.get("id"))
            .where(
                builder.equal(le.get("payment").get("id"), root.get("id")),
                builder.equal(le.get("entryType"), EntryType.CREDIT),
                builder.equal(le.get("account").get("accountNumber"), receiverAccountNumber));
        predicates.add(builder.exists(sub));
      }

      if (status != null) {
        predicates.add(builder.equal(root.get("status"), status));
      }
      return builder.and(predicates);
    };
  }
}
