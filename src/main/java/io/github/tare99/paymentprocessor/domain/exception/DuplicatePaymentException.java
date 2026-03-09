package io.github.tare99.paymentprocessor.domain.exception;

public class DuplicatePaymentException extends RuntimeException {

  public DuplicatePaymentException(String message) {
    super(message);
  }
}
