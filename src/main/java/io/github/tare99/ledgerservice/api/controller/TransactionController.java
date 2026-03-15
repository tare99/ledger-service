package io.github.tare99.ledgerservice.api.controller;

import io.github.tare99.ledgerservice.api.request.CreateTransactionRequest;
import io.github.tare99.ledgerservice.api.response.TransactionResponse;
import io.github.tare99.ledgerservice.domain.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PostMapping
  public ResponseEntity<TransactionResponse> createTransaction(
      @Valid @RequestBody CreateTransactionRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(transactionService.createTransaction(request));
  }

  @PostMapping("/{id}/reverse")
  public ResponseEntity<TransactionResponse> reverseTransaction(@PathVariable String id) {
    return ResponseEntity.ok(transactionService.reverseTransaction(id));
  }

  @GetMapping("/{id}")
  public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String id) {
    return ResponseEntity.ok(transactionService.getTransaction(id));
  }
}
