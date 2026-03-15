package io.github.tare99.ledgerservice.api.advice;

import io.github.tare99.ledgerservice.api.response.ErrorResponse;
import io.github.tare99.ledgerservice.domain.exception.AccountNotFoundException;
import io.github.tare99.ledgerservice.domain.exception.CurrencyMismatchException;
import io.github.tare99.ledgerservice.domain.exception.InsufficientFundsException;
import io.github.tare99.ledgerservice.domain.exception.TransactionNotFoundException;
import io.github.tare99.ledgerservice.domain.exception.UnbalancedTransactionException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LedgerControllerAdvice {

  private static final Logger log = LoggerFactory.getLogger(LedgerControllerAdvice.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse(ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, "Validation Failed", message);
  }

  @ExceptionHandler(TransactionNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleTransactionNotFound(
      TransactionNotFoundException ex) {
    return error(HttpStatus.NOT_FOUND, "Transaction Not Found", ex.getMessage());
  }

  @ExceptionHandler(AccountNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
    return error(HttpStatus.NOT_FOUND, "Account Not Found", ex.getMessage());
  }

  @ExceptionHandler(InsufficientFundsException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
    return error(HttpStatus.UNPROCESSABLE_CONTENT, "Insufficient Funds", ex.getMessage());
  }

  @ExceptionHandler(CurrencyMismatchException.class)
  public ResponseEntity<ErrorResponse> handleCurrencyMismatch(CurrencyMismatchException ex) {
    return error(HttpStatus.UNPROCESSABLE_CONTENT, "Currency Mismatch", ex.getMessage());
  }

  @ExceptionHandler(UnbalancedTransactionException.class)
  public ResponseEntity<ErrorResponse> handleUnbalanced(UnbalancedTransactionException ex) {
    return error(HttpStatus.UNPROCESSABLE_CONTENT, "Unbalanced Transaction", ex.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
    return error(HttpStatus.BAD_REQUEST, "Invalid State", ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    return error(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
    log.error("Unhandled exception", ex);
    return error(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        "An unexpected error occurred");
  }

  private ResponseEntity<ErrorResponse> error(HttpStatus status, String error, String message) {
    return ResponseEntity.status(status)
        .body(new ErrorResponse(status.value(), error, message, Instant.now()));
  }
}