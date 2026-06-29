CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE prescriptions
    ADD COLUMN IF NOT EXISTS uuid_id UUID;

UPDATE prescriptions
SET uuid_id = gen_random_uuid()
WHERE uuid_id IS NULL;

ALTER TABLE prescriptions
    ALTER COLUMN uuid_id SET NOT NULL;

ALTER TABLE prescriptions
    DROP CONSTRAINT IF EXISTS prescriptions_pkey;

ALTER TABLE prescriptions
    DROP COLUMN IF EXISTS id;

ALTER TABLE prescriptions
    RENAME COLUMN uuid_id TO id;

ALTER TABLE prescriptions
    ADD CONSTRAINT prescriptions_pkey PRIMARY KEY (id);
