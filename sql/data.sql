INSERT INTO users (email, password_hash, balance_cents)
VALUES
    ('alice@test.com', 'HASHED_PASSWORD', 100000),
    ('bob@test.com',   'HASHED_PASSWORD',  50000),
    ('charlie@test.com','HASHED_PASSWORD', 250000);

-- Connections: Alice knows Bob, Alice knows Charlie
INSERT INTO connections (user_id, friend_id)
VALUES
    (1, 2),
    (1, 3);

-- Bank accounts (V1)
INSERT INTO bank_accounts (user_id, iban, label)
VALUES
    (1, 'FR7630006000011234567890189', 'Compte courant Alice'),
    (2, 'FR7630006000019876543210123', 'Compte courant Bob');

-- Transactions (internal transfer example with 0.5% fee)
-- Example: Alice -> Bob : 2000 cents, fee 10 cents (0.5%)
INSERT INTO transactions (type, sender_user_id, receiver_user_id, amount_cents, fee_cents, description)
VALUES
    ('INTERNAL', 1, 2, 2000, 10, 'Remboursement déjeuner');

-- Top up example
INSERT INTO transactions (type, receiver_user_id, bank_account_id, amount_cents, fee_cents, description)
VALUES
    ('TOPUP', 1, 1, 5000, 0, 'Recharge compte');