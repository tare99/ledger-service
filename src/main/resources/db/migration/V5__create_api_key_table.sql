CREATE TABLE api_key
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    key_hash     VARCHAR(64)  NOT NULL UNIQUE,
    name         VARCHAR(100) NOT NULL,
    account_id   BIGINT       NOT NULL REFERENCES account (id),
    active       BOOLEAN      NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    last_used_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_api_key_account_id ON api_key (account_id);
CREATE INDEX idx_api_key_created_at ON api_key (created_at);
