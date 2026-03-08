CREATE TABLE payment
(
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_id          VARCHAR(30)    NOT NULL UNIQUE,
    sender_account_id   BIGINT         NOT NULL REFERENCES account (id),
    receiver_account_id BIGINT         NOT NULL REFERENCES account (id),
    amount              DECIMAL(15, 2) NOT NULL,
    currency            VARCHAR(3)     NOT NULL,
    status              VARCHAR(20)    NOT NULL,
    description         VARCHAR(255),
    idempotency_key     VARCHAR(64)    NOT NULL UNIQUE,
    risk_score          DOUBLE PRECISION,
    client_ip           VARCHAR(45),
    created_at          TIMESTAMP      NOT NULL,
    updated_at          TIMESTAMP      NOT NULL
);

CREATE INDEX idx_payment_idempotency_key ON payment (idempotency_key);
CREATE INDEX idx_payment_sender_account_id ON payment (sender_account_id);
CREATE INDEX idx_payment_receiver_account_id ON payment (receiver_account_id);
CREATE INDEX idx_payment_id ON payment (payment_id);
CREATE INDEX idx_payment_sender_status_created
    ON payment (sender_account_id, status, created_at);
CREATE INDEX idx_payment_sender_status
    ON payment (sender_account_id, status);