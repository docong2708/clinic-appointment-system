create table patients (
  id bigint primary key generated always as identity,
  first_name text,
  last_name text,
  date_of_birth date,
  gender text,
  contact_information text
);

create table medicalrecords (
  id bigint primary key generated always as identity,
  patient_id bigint references patients (id),
  record_date date,
  diagnosis text,
  treatment text,
  notes text
);

create table appointments (
  id bigint primary key generated always as identity,
  patient_id bigint references patients (id),
  appointment_date timestamp,
  doctor_name text,
  department text,
  status text
);

create table prescriptions (
  id bigint primary key generated always as identity,
  medical_record_id bigint references medicalrecords (id),
  medication_name text,
  dosage text,
  frequency text,
  duration text
);

create table doctors (
  id bigint primary key generated always as identity,
  first_name text,
  last_name text,
  specialization text,
  contact_information text
);
