CREATE TABLE payment
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_id      VARCHAR(30) NOT NULL UNIQUE,
    status          VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(64) NOT NULL UNIQUE,
    created_at      TIMESTAMP   NOT NULL,
    updated_at      TIMESTAMP   NOT NULL
);

CREATE INDEX idx_payment_idempotency_key ON payment (idempotency_key);
CREATE INDEX idx_payment_id ON payment (payment_id);
CREATE INDEX idx_payment_created_at ON payment (created_at);