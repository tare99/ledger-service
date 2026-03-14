package io.github.tare99.paymentprocessor.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.tare99.paymentprocessor.api.controller.PaymentController;
import io.github.tare99.paymentprocessor.api.request.CreatePaymentRequest;
import io.github.tare99.paymentprocessor.api.request.Currency;
import io.github.tare99.paymentprocessor.api.request.PaymentStatus;
import io.github.tare99.paymentprocessor.api.response.CreatePaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaginatedPaymentResponse.PaginationResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentResponse;
import io.github.tare99.paymentprocessor.api.response.PaymentStatusResponse;
import io.github.tare99.paymentprocessor.api.response.RefundPaymentResponse;
import io.github.tare99.paymentprocessor.domain.exception.AccountNotFoundException;
import io.github.tare99.paymentprocessor.domain.exception.PaymentNotFoundException;
import io.github.tare99.paymentprocessor.domain.exception.UnauthorizedPaymentAccessException;
import io.github.tare99.paymentprocessor.security.ApiKeyAuthenticationFilter;
import io.github.tare99.paymentprocessor.security.UserPrincipal;
import io.github.tare99.paymentprocessor.domain.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

  private static final String ALICE = "ACC-ALICE00000000001";
  private static final String BOB = "ACC-BOB000000000002";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private PaymentService paymentService;
  @MockitoBean private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

  @BeforeEach
  void setUpAuth() {
    authenticateAs(ALICE);
  }

  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createPaymentReturns201() throws Exception {
    var request =
        new CreatePaymentRequest("req-1", ALICE, BOB, new BigDecimal("100.00"), Currency.USD);
    var response =
        new CreatePaymentResponse(
            "PAY-123", ALICE, BOB, new BigDecimal("100.00"), Currency.USD, PaymentStatus.COMPLETED);

    when(paymentService.createPayment(any(), eq(ALICE))).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.paymentId").value("PAY-123"))
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  void createPaymentWithMissingFieldsReturns400() throws Exception {
    String body =
        """
        {"senderAccountId": "ACC-ALICE00000000001"}
        """;

    mockMvc
        .perform(post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getPaymentReturnsPayment() throws Exception {
    var response =
        new PaymentResponse(
            "PAY-123",
            ALICE,
            BOB,
            new BigDecimal("100.00"),
            Currency.USD,
            PaymentStatus.COMPLETED,
            Instant.now(),
            Instant.now());

    when(paymentService.getPayment("PAY-123", ALICE)).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/payments/PAY-123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentId").value("PAY-123"));
  }

  @Test
  void getPaymentNotFoundReturns404() throws Exception {
    when(paymentService.getPayment("PAY-999", ALICE))
        .thenThrow(new PaymentNotFoundException("Payment not found"));

    mockMvc
        .perform(get("/api/v1/payments/PAY-999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Payment Not Found"));
  }

  @Test
  void listPaymentsReturnsPaginatedResponse() throws Exception {
    var pagination = new PaginationResponse(0, 10, 1, 1);
    var payment =
        new PaymentResponse(
            "PAY-123",
            ALICE,
            BOB,
            new BigDecimal("100.00"),
            Currency.USD,
            PaymentStatus.COMPLETED,
            Instant.now(),
            Instant.now());
    var response = new PaginatedPaymentResponse(List.of(payment), pagination);

    when(paymentService.listPayments(null, null, null, 0, 10, ALICE)).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/payments").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.payments").isArray())
        .andExpect(jsonPath("$.payments.length()").value(1))
        .andExpect(jsonPath("$.pagination.totalElements").value(1));
  }

  @Test
  void refundPaymentReturnsOk() throws Exception {
    var response =
        new RefundPaymentResponse(
            "PAY-123", new BigDecimal("100.00"), Currency.USD, PaymentStatus.REFUNDED);

    when(paymentService.refundPayment("PAY-123", ALICE)).thenReturn(response);

    mockMvc
        .perform(post("/api/v1/payments/PAY-123/refund"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentId").value("PAY-123"))
        .andExpect(jsonPath("$.refundedAmount").value(100.00))
        .andExpect(jsonPath("$.status").value("REFUNDED"));
  }

  @Test
  void createPaymentWithNonExistentAccountReturns404() throws Exception {
    authenticateAs("ACC-NONEXISTENT00001");
    var request =
        new CreatePaymentRequest(
            "req-1", "ACC-NONEXISTENT00001", BOB, new BigDecimal("100.00"), Currency.USD);

    when(paymentService.createPayment(any(), eq("ACC-NONEXISTENT00001")))
        .thenThrow(new AccountNotFoundException("Account not found: ACC-NONEXISTENT00001"));

    mockMvc
        .perform(
            post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Account Not Found"))
        .andExpect(jsonPath("$.message").value("Account not found: ACC-NONEXISTENT00001"));
  }

  @Test
  void getPaymentStatusReturnsStatus() throws Exception {
    var response = new PaymentStatusResponse("PAY-123", PaymentStatus.COMPLETED);

    when(paymentService.getPaymentStatus("PAY-123", ALICE)).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/payments/PAY-123/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  void createPaymentFromOtherAccountReturns403() throws Exception {
    authenticateAs(BOB);
    var request =
        new CreatePaymentRequest("req-1", ALICE, BOB, new BigDecimal("100.00"), Currency.USD);

    when(paymentService.createPayment(any(), eq(BOB)))
        .thenThrow(
            new UnauthorizedPaymentAccessException(
                "You can only create payments from your own account"));

    mockMvc
        .perform(
            post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("Forbidden"));
  }

  private void authenticateAs(String accountNumber) {
    UserPrincipal principal = new UserPrincipal(accountNumber);
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    var context = new SecurityContextImpl(auth);
    SecurityContextHolder.setContext(context);
  }
}