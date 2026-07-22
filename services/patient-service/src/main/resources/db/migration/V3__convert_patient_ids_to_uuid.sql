CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS uuid_id UUID;

UPDATE patients
SET uuid_id = gen_random_uuid()
WHERE uuid_id IS NULL;

ALTER TABLE patients
    ALTER COLUMN uuid_id SET NOT NULL;

ALTER TABLE medicalrecords
    ADD COLUMN IF NOT EXISTS patient_uuid UUID;

UPDATE medicalrecords medical_record
SET patient_uuid = patient.uuid_id
FROM patients patient
WHERE medical_record.patient_id = patient.id
  AND medical_record.patient_uuid IS NULL;

ALTER TABLE medicalrecords
    DROP CONSTRAINT IF EXISTS medicalrecords_patient_id_fkey;

ALTER TABLE patients
    DROP CONSTRAINT IF EXISTS patients_pkey;

ALTER TABLE medicalrecords
    DROP COLUMN IF EXISTS patient_id;

ALTER TABLE patients
    DROP COLUMN IF EXISTS id;

ALTER TABLE patients
    RENAME COLUMN uuid_id TO id;

ALTER TABLE medicalrecords
    RENAME COLUMN patient_uuid TO patient_id;

ALTER TABLE patients
    ADD CONSTRAINT patients_pkey PRIMARY KEY (id);

ALTER TABLE medicalrecords
    ADD CONSTRAINT medicalrecords_patient_id_fkey
        FOREIGN KEY (patient_id) REFERENCES patients (id);
