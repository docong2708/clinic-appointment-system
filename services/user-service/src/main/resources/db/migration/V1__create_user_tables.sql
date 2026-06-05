CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(30) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED', 'PENDING_VERIFY'))
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_phone_number ON users (phone_number);
CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);

INSERT INTO roles (name, description)
VALUES
    ('ADMIN', 'System administrator'),
    ('DOCTOR', 'Doctor user'),
    ('PATIENT', 'Patient user')
ON CONFLICT (name) DO NOTHING;
