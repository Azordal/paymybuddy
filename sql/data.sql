-- ============================
-- USERS (password = "password123")
-- BCrypt hash generated once
-- ============================

INSERT INTO users (email, password_hash, balance_cents) VALUES
    ('alice@test.com',   '$2a$10$e0MYzXyjpJS2n4l6pC1ZFeZb7MZbY7Rj7PjQ0PZ7mZp5W5mGJv9yW', 100000),
    ('bob@test.com',     '$2a$10$e0MYzXyjpJS2n4l6pC1ZFeZb7MZbY7Rj7PjQ0PZ7mZp5W5mGJv9yW',  50000),
    ('charlie@test.com', '$2a$10$e0MYzXyjpJS2n4l6pC1ZFeZb7MZbY7Rj7PjQ0PZ7mZp5W5mGJv9yW', 250000);

-- ============================
-- CONNECTIONS (friends)
-- ============================

INSERT INTO connections (user_id, friend_id) VALUES
    (1, 2),
    (1, 3),
    (2, 3);

-- ============================
-- BANK ACCOUNTS
-- ============================

INSERT INTO bank_accounts (user_id, iban, label) VALUES
    (1, 'FR7630006000011234567890189', 'Compte courant Alice'),
    (2, 'FR7630006000019876543210123', 'Compte courant Bob'),
    (3, 'FR7630006000010000000000999', 'Compte épargne Charlie');

-- ============================
-- TRANSACTIONS
-- ============================

-- Alice → Bob (internal transfer, fee 0.5%)
INSERT INTO transactions
(type, sender_user_id, receiver_user_id, amount_cents, fee_cents, description)
VALUES
    ('INTERNAL', 1, 2, 2000, 10, 'Remboursement déjeuner');

-- Bob → Charlie
INSERT INTO transactions
(type, sender_user_id, receiver_user_id, amount_cents, fee_cents, description)
VALUES
    ('INTERNAL', 2, 3, 5000, 25, 'Cadeau anniversaire');

-- Alice topup from bank
INSERT INTO transactions
(type, receiver_user_id, bank_account_id, amount_cents, fee_cents, description)
VALUES
    ('TOPUP', 1, 1, 10000, 0, 'Recharge initiale');

-- Charlie withdraw
INSERT INTO transactions
(type, sender_user_id, bank_account_id, amount_cents, fee_cents, description)
VALUES
    ('WITHDRAW', 3, 3, 30000, 0, 'Virement vers banque');