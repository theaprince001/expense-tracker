
ALTER TABLE users
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS activation_token VARCHAR(500),
ADD COLUMN IF NOT EXISTS activation_token_expiry TIMESTAMP;

-- Make activation_token unique
ALTER TABLE users ADD CONSTRAINT uk_activation_token UNIQUE (activation_token);

-- Update existing users
UPDATE users SET is_active = TRUE WHERE is_active IS NULL;

COMMENT ON COLUMN users.is_active IS 'Account activation status';
COMMENT ON COLUMN users.activation_token IS 'One-time activation token';
COMMENT ON COLUMN users.activation_token_expiry IS 'Token expiry (24h)';
