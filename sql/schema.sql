CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    balance_cents BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE connections (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT fk_conn_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conn_friend FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE bank_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    iban VARCHAR(34) NOT NULL,
    label VARCHAR(120),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bank_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_iban UNIQUE (user_id, iban)
);

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL, -- INTERNAL / TOPUP / WITHDRAW
    sender_user_id BIGINT NULL,
    receiver_user_id BIGINT NULL,
    bank_account_id BIGINT NULL,
    amount_cents BIGINT NOT NULL,
    fee_cents BIGINT NOT NULL DEFAULT 0,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tx_sender FOREIGN KEY (sender_user_id) REFERENCES users(id),
    CONSTRAINT fk_tx_receiver FOREIGN KEY (receiver_user_id) REFERENCES users(id),
    CONSTRAINT fk_tx_bank FOREIGN KEY (bank_account_id) REFERENCES bank_accounts(id)
);

CREATE INDEX idx_tx_created ON transactions(created_at);
CREATE INDEX idx_tx_sender ON transactions(sender_user_id);
CREATE INDEX idx_tx_receiver ON transactions(receiver_user_id);