INSERT INTO account (account_number, balance, currency, created_at,
                     updated_at)
VALUES ('ACC-ALICE00000000001', 10000.00, 'USD',
        NOW(), NOW()),
       ('ACC-BOB000000000002', 5000.00, 'USD', NOW(),
        NOW()),
       ('ACC-CAROL00000000003', 2500.00, 'EUR',
        NOW(), NOW()),
       ('ACC-DAVE00000000004', 500.00, 'GBP', NOW(),
        NOW());
