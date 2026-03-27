CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    telegram_charge_id VARCHAR(255) UNIQUE,
    provider_charge_id VARCHAR(255),
    amount INTEGER NOT NULL,
    currency VARCHAR(10) DEFAULT 'UZS',
    status VARCHAR(20) DEFAULT 'PENDING',
    invoice_payload VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_telegram_charge_id ON payments(telegram_charge_id);
