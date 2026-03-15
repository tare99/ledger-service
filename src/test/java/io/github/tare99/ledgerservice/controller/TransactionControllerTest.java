package io.github.tare99.ledgerservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.tare99.ledgerservice.api.controller.TransactionController;
import io.github.tare99.ledgerservice.api.request.CreateTransactionRequest;
import io.github.tare99.ledgerservice.api.request.CreateTransactionRequest.EntryInstruction;
import io.github.tare99.ledgerservice.api.response.LedgerEntryResponse;
import io.github.tare99.ledgerservice.api.response.TransactionResponse;
import io.github.tare99.ledgerservice.domain.entity.EntryType;
import io.github.tare99.ledgerservice.domain.entity.TransactionStatus;
import io.github.tare99.ledgerservice.domain.exception.TransactionNotFoundException;
import io.github.tare99.ledgerservice.domain.exception.UnbalancedTransactionException;
import io.github.tare99.ledgerservice.domain.service.TransactionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

  private static final String ALICE = "ACC-ALICE00000000001";
  private static final String BOB = "ACC-BOB000000000002";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private TransactionService transactionService;

  @Test
  void createTransactionReturns201() throws Exception {
    var request =
        new CreateTransactionRequest(
            "req-1",
            "test payment",
            List.of(
                new EntryInstruction(ALICE, EntryType.DEBIT, new BigDecimal("100.00")),
                new EntryInstruction(BOB, EntryType.CREDIT, new BigDecimal("100.00"))));
    var response =
        new TransactionResponse(
            "TXN-123",
            TransactionStatus.POSTED,
            "test payment",
            List.of(
                new LedgerEntryResponse(
                    ALICE, EntryType.DEBIT, new BigDecimal("100.00"), new BigDecimal("9900.00")),
                new LedgerEntryResponse(
                    BOB, EntryType.CREDIT, new BigDecimal("100.00"), new BigDecimal("5100.00"))),
            Instant.now());

    when(transactionService.createTransaction(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.transactionId").value("TXN-123"))
        .andExpect(jsonPath("$.status").value("POSTED"))
        .andExpect(jsonPath("$.entries.length()").value(2));
  }

  @Test
  void createTransactionWithMissingFieldsReturns400() throws Exception {
    String body =
        """
        {"description": "test"}
        """;

    mockMvc
        .perform(post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getTransactionReturnsTransaction() throws Exception {
    var response =
        new TransactionResponse(
            "TXN-123",
            TransactionStatus.POSTED,
            "test",
            List.of(
                new LedgerEntryResponse(
                    ALICE, EntryType.DEBIT, new BigDecimal("100.00"), new BigDecimal("9900.00")),
                new LedgerEntryResponse(
                    BOB, EntryType.CREDIT, new BigDecimal("100.00"), new BigDecimal("5100.00"))),
            Instant.now());

    when(transactionService.getTransaction("TXN-123")).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/transactions/TXN-123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.transactionId").value("TXN-123"));
  }

  @Test
  void getTransactionNotFoundReturns404() throws Exception {
    when(transactionService.getTransaction("TXN-999"))
        .thenThrow(new TransactionNotFoundException("Transaction not found"));

    mockMvc
        .perform(get("/api/v1/transactions/TXN-999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Transaction Not Found"));
  }

  @Test
  void reverseTransactionReturnsOk() throws Exception {
    var response =
        new TransactionResponse(
            "TXN-123",
            TransactionStatus.REVERSED,
            "test",
            List.of(
                new LedgerEntryResponse(
                    ALICE, EntryType.DEBIT, new BigDecimal("100.00"), new BigDecimal("9900.00")),
                new LedgerEntryResponse(
                    BOB, EntryType.CREDIT, new BigDecimal("100.00"), new BigDecimal("5100.00")),
                new LedgerEntryResponse(
                    ALICE, EntryType.CREDIT, new BigDecimal("100.00"), new BigDecimal("10000.00")),
                new LedgerEntryResponse(
                    BOB, EntryType.DEBIT, new BigDecimal("100.00"), new BigDecimal("5000.00"))),
            Instant.now());

    when(transactionService.reverseTransaction("TXN-123")).thenReturn(response);

    mockMvc
        .perform(post("/api/v1/transactions/TXN-123/reverse"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REVERSED"))
        .andExpect(jsonPath("$.entries.length()").value(4));
  }

  @Test
  void createUnbalancedTransactionReturns422() throws Exception {
    when(transactionService.createTransaction(any()))
        .thenThrow(
            new UnbalancedTransactionException(
                "Total debits (100.00) must equal total credits (50.00)"));

    var request =
        new CreateTransactionRequest(
            "req-1",
            "unbalanced",
            List.of(
                new EntryInstruction(ALICE, EntryType.DEBIT, new BigDecimal("100.00")),
                new EntryInstruction(BOB, EntryType.CREDIT, new BigDecimal("50.00"))));

    mockMvc
        .perform(
            post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.error").value("Unbalanced Transaction"));
  }
}
