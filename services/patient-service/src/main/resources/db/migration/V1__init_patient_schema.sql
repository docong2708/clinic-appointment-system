-- Create patients table
CREATE TABLE IF NOT EXISTS patients (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    first_name TEXT,
    last_name TEXT,
    date_of_birth DATE,
    gender TEXT,
    contact_information TEXT
);

-- Create medicalrecords table
CREATE TABLE IF NOT EXISTS medicalrecords (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    patient_id BIGINT REFERENCES patients (id),
    record_date DATE,
    diagnosis TEXT,
    treatment TEXT,
    notes TEXT
);

-- Create prescriptions table
CREATE TABLE IF NOT EXISTS prescriptions (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    medical_record_id BIGINT REFERENCES medicalrecords (id),
    medication_name TEXT,
    dosage TEXT,
    frequency TEXT,
    duration TEXT
);
