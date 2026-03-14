package io.github.tare99.paymentprocessor.api.advice;

import io.github.tare99.paymentprocessor.api.response.ErrorResponse;
import io.github.tare99.paymentprocessor.domain.exception.AccountNotFoundException;
import io.github.tare99.paymentprocessor.domain.exception.CurrencyMismatchException;
import io.github.tare99.paymentprocessor.domain.exception.DuplicatePaymentException;
import io.github.tare99.paymentprocessor.domain.exception.InsufficientFundsException;
import io.github.tare99.paymentprocessor.domain.exception.PaymentNotFoundException;
import io.github.tare99.paymentprocessor.domain.exception.UnauthorizedPaymentAccessException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentControllerAdvice {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse(ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, "Validation Failed", message);
  }

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex) {
    return error(HttpStatus.NOT_FOUND, "Payment Not Found", ex.getMessage());
  }

  @ExceptionHandler(AccountNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
    return error(HttpStatus.NOT_FOUND, "Account Not Found", ex.getMessage());
  }

  @ExceptionHandler(UnauthorizedPaymentAccessException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
      UnauthorizedPaymentAccessException ex) {
    return error(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage());
  }

  @ExceptionHandler(InsufficientFundsException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
    return error(HttpStatus.UNPROCESSABLE_CONTENT, "Insufficient Funds", ex.getMessage());
  }

  @ExceptionHandler(CurrencyMismatchException.class)
  public ResponseEntity<ErrorResponse> handleCurrencyMismatch(CurrencyMismatchException ex) {
    return error(HttpStatus.UNPROCESSABLE_CONTENT, "Currency Mismatch", ex.getMessage());
  }

  @ExceptionHandler(DuplicatePaymentException.class)
  public ResponseEntity<ErrorResponse> handleDuplicate(DuplicatePaymentException ex) {
    return error(HttpStatus.CONFLICT, "Duplicate Payment", ex.getMessage());
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
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
  }

  private ResponseEntity<ErrorResponse> error(HttpStatus status, String error, String message) {
    return ResponseEntity.status(status)
        .body(new ErrorResponse(status.value(), error, message, Instant.now()));
  }
}
