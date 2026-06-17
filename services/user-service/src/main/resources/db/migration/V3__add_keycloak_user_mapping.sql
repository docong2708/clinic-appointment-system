ALTER TABLE users ADD COLUMN IF NOT EXISTS keycloak_user_id VARCHAR(64);
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_keycloak_user_id ON users (keycloak_user_id);

DROP TABLE IF EXISTS refresh_tokens;
