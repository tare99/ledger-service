CREATE TABLE account (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_number VARCHAR(20)     NOT NULL UNIQUE,
    owner_name  VARCHAR(100)        NOT NULL,
    email       VARCHAR(255)        NOT NULL,
    balance     DECIMAL(15, 2)      NOT NULL DEFAULT 0.00,
    currency    VARCHAR(3)          NOT NULL,
    status      VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP           NOT NULL,
    updated_at  TIMESTAMP           NOT NULL,
    version     BIGINT              NOT NULL DEFAULT 0
);

CREATE INDEX idx_account_account_number ON account (account_number);
CREATE INDEX idx_account_email          ON account (email);
