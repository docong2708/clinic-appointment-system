CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE medicalrecords
    ADD COLUMN IF NOT EXISTS uuid_id UUID;

UPDATE medicalrecords
SET uuid_id = gen_random_uuid()
WHERE uuid_id IS NULL;

ALTER TABLE medicalrecords
    ALTER COLUMN uuid_id SET NOT NULL;

ALTER TABLE prescriptions
    ADD COLUMN IF NOT EXISTS medical_record_uuid UUID;

UPDATE prescriptions prescription
SET medical_record_uuid = medical_record.uuid_id
FROM medicalrecords medical_record
WHERE prescription.medical_record_id = medical_record.id
  AND prescription.medical_record_uuid IS NULL;

DO $$
DECLARE
    fk_name TEXT;
BEGIN
    FOR fk_name IN
        SELECT constraint_info.conname
        FROM pg_constraint constraint_info
        JOIN pg_class source_table ON source_table.oid = constraint_info.conrelid
        JOIN pg_class target_table ON target_table.oid = constraint_info.confrelid
        WHERE constraint_info.contype = 'f'
          AND source_table.relname = 'prescriptions'
          AND target_table.relname = 'medicalrecords'
    LOOP
        EXECUTE format('ALTER TABLE prescriptions DROP CONSTRAINT IF EXISTS %I', fk_name);
    END LOOP;
END $$;

ALTER TABLE medicalrecords
    DROP CONSTRAINT IF EXISTS medicalrecords_pkey;

ALTER TABLE prescriptions
    DROP COLUMN IF EXISTS medical_record_id;

ALTER TABLE medicalrecords
    DROP COLUMN IF EXISTS id;

ALTER TABLE medicalrecords
    RENAME COLUMN uuid_id TO id;

ALTER TABLE prescriptions
    RENAME COLUMN medical_record_uuid TO medical_record_id;

ALTER TABLE medicalrecords
    ADD CONSTRAINT medicalrecords_pkey PRIMARY KEY (id);

ALTER TABLE prescriptions
    ADD CONSTRAINT prescriptions_medical_record_id_fkey
        FOREIGN KEY (medical_record_id) REFERENCES medicalrecords (id);
