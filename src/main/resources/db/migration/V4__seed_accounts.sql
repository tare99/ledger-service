INSERT INTO account (account_number, balance, currency, account_type, created_at, updated_at)
VALUES ('ACC-ALICE00000000001', 10000.00, 'USD', 'LIABILITY', NOW(), NOW()),
       ('ACC-BOB000000000002', 5000.00, 'USD', 'LIABILITY', NOW(), NOW()),
       ('ACC-CAROL00000000003', 2500.00, 'EUR', 'LIABILITY', NOW(), NOW()),
       ('ACC-DAVE00000000004', 500.00, 'GBP', 'LIABILITY', NOW(), NOW());

INSERT INTO account (account_number, balance, currency, account_type, created_at, updated_at)
VALUES ('SYS-FUNDING-USD', 0.00, 'USD', 'ASSET', NOW(), NOW()),
       ('SYS-FUNDING-EUR', 0.00, 'EUR', 'ASSET', NOW(), NOW()),
       ('SYS-FUNDING-GBP', 0.00, 'GBP', 'ASSET', NOW(), NOW());
