package io.github.tare99.paymentprocessor.domain.exception;

public class UnauthorizedPaymentAccessException extends RuntimeException {

  public UnauthorizedPaymentAccessException(String message) {
    super(message);
  }
}
