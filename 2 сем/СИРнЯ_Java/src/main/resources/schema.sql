CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS otp_config (
    id SMALLINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    code_length INT NOT NULL CHECK (code_length BETWEEN 4 AND 10),
    ttl_seconds INT NOT NULL CHECK (ttl_seconds BETWEEN 30 AND 600),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO otp_config (id, code_length, ttl_seconds)
VALUES (1, 6, 120)
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS otp_codes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    operation_id VARCHAR(120) NOT NULL UNIQUE,
    code_hash VARCHAR(255) NOT NULL,
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('email', 'sms', 'telegram', 'file')),
    destination VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'EXPIRED', 'USED')),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    used_at TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_otp_codes_user_id ON otp_codes(user_id);
CREATE INDEX IF NOT EXISTS idx_otp_codes_status_expires_at ON otp_codes(status, expires_at);
