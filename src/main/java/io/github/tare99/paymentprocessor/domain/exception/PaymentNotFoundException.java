package io.github.tare99.paymentprocessor.domain.exception;

public class PaymentNotFoundException extends RuntimeException {

  public PaymentNotFoundException(String message) {
    super(message);
  }
}
