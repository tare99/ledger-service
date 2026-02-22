package io.github.tare99.paymentprocessor.api.response;

import java.time.Instant;

public record ErrorResponse(int status, String error, String message, Instant timestamp) {}
