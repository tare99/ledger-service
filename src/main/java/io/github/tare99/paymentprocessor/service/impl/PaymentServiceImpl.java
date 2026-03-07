package io.github.tare99.paymentprocessor.service.impl;

import io.github.tare99.paymentprocessor.api.request.CreatePaymentRequest;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import io.github.tare99.paymentprocessor.api.response.CancelPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.CreatePaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentStatusResponse;
import io.github.tare99.paymentprocessor.entity.Account;
import io.github.tare99.paymentprocessor.entity.Payment;
import io.github.tare99.paymentprocessor.exception.PaymentNotFoundException;
import io.github.tare99.paymentprocessor.mapper.PaymentMapper;
import io.github.tare99.paymentprocessor.repository.PaymentRepository;
import io.github.tare99.paymentprocessor.service.AccountService;
import io.github.tare99.paymentprocessor.service.AuditService;
import io.github.tare99.paymentprocessor.service.PaymentService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
  private final AuditService auditService;
  private final PaymentMapper paymentMapper;

  public PaymentServiceImpl(
      PaymentRepository paymentRepository,
      AccountService accountService,
      AuditService auditService,
      PaymentMapper paymentMapper) {
    this.paymentRepository = paymentRepository;
    this.accountService = accountService;
    this.auditService = auditService;
    this.paymentMapper = paymentMapper;
  }

  @Override
  @Transactional
  public CreatePaymentResponse createPayment(CreatePaymentRequest request, String clientIp) {
    Optional<Payment> optionalPayment = paymentRepository.findByIdempotencyKey(request.requestId());
    if (optionalPayment.isPresent()) {
      log.info("Payment with request id {} already exists, will return OK", request.requestId());
      return paymentMapper.toCreatePaymentResponse(optionalPayment.get());
    }

    Account sender = accountService.getActiveAccountByNumber(request.senderAccountId());
    Account receiver = accountService.getActiveAccountByNumber(request.receiverAccountId());

    if (sender.getId().equals(receiver.getId())) {
      throw new IllegalArgumentException("Sender and receiver accounts must be different");
    }

    BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
    sender.debit(amount);
    receiver.credit(amount);

    Payment payment =
        Payment.builder()
            .senderAccount(sender)
            .receiverAccount(receiver)
            .amount(amount)
            .currency(request.currency())
            .status(PaymentStatus.COMPLETED)
            .idempotencyKey(request.requestId())
            .clientIp(clientIp)
            .build();

    paymentRepository.insert(payment);

    log.info(
        "Payment created: paymentId={} amount={} currency={}",
        payment.getPaymentId(),
        amount,
        request.currency());
    return paymentMapper.toCreatePaymentResponse(payment);
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentResponse getPayment(String paymentId) {
    Payment payment = getPaymentById(paymentId);
    return paymentMapper.toPaymentResponse(payment);
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
    return paymentMapper.toPaginatedResponse(resultPage);
  }

  @Override
  @Transactional
  public CancelPaymentResponse cancelPayment(String paymentId) {
    Payment payment = getPaymentById(paymentId);
    if (payment.getStatus() != PaymentStatus.PENDING) {
      throw new IllegalStateException(
          "Cannot cancel payment in state: "
              + payment.getStatus()
              + ". Only PENDING payments can be cancelled.");
    }

    Account sender = payment.getSenderAccount();
    Account receiver = payment.getReceiverAccount();
    BigDecimal amount = payment.getAmount();

    sender.credit(amount);
    receiver.debit(amount);

    paymentRepository.updateStatus(payment.getId(), PaymentStatus.CANCELLED);

    log.info("Payment cancelled: paymentId={}", payment.getId());
    auditService.log(
        payment.getId(), "PAYMENT_CANCELLED", "{\"reason\":\"Cancelled by request\"}", "SYSTEM");

    return new CancelPaymentResponse("Payment " + paymentId + " cancelled successfully");
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentStatusResponse getPaymentStatus(String paymentId) {
    Payment payment = getPaymentById(paymentId);
    return new PaymentStatusResponse(paymentId, payment.getStatus());
  }

  private Payment getPaymentById(String paymentId) {
    Optional<Payment> optionalPayment = paymentRepository.findByPaymentId(paymentId);
    if (optionalPayment.isEmpty()) {
      throw new PaymentNotFoundException("Payment not found");
    }
    return optionalPayment.get();
  }

  @SuppressWarnings("unchecked")
  private Specification<Payment> buildSpecification(
      String senderAccountNumber, String receiverAccountNumber, PaymentStatus status) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      boolean isCountQuery = Long.class.equals(query.getResultType());

      if (!isCountQuery) {
        var senderFetch =
            (Join<Payment, Account>) (Join<?, ?>) root.fetch("senderAccount", JoinType.INNER);
        var receiverFetch =
            (Join<Payment, Account>) (Join<?, ?>) root.fetch("receiverAccount", JoinType.INNER);

        if (senderAccountNumber != null && !senderAccountNumber.isBlank()) {
          predicates.add(builder.equal(senderFetch.get("accountNumber"), senderAccountNumber));
        }
        if (receiverAccountNumber != null && !receiverAccountNumber.isBlank()) {
          predicates.add(builder.equal(receiverFetch.get("accountNumber"), receiverAccountNumber));
        }
      } else {
        if (senderAccountNumber != null && !senderAccountNumber.isBlank()) {
          Join<Payment, Account> senderJoin = root.join("senderAccount", JoinType.INNER);
          predicates.add(builder.equal(senderJoin.get("accountNumber"), senderAccountNumber));
        }
        if (receiverAccountNumber != null && !receiverAccountNumber.isBlank()) {
          Join<Payment, Account> receiverJoin = root.join("receiverAccount", JoinType.INNER);
          predicates.add(builder.equal(receiverJoin.get("accountNumber"), receiverAccountNumber));
        }
      }

      if (status != null) {
        predicates.add(builder.equal(root.get("status"), status));
      }
      return builder.and(predicates);
    };
  }
}
