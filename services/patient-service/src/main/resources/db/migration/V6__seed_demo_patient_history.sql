CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO patients (id, user_id, first_name, last_name, date_of_birth, gender, contact_information)
VALUES
    (
        '9f111111-1111-4111-8111-111111111111',
        NULL,
        'Minh',
        'Demo Legacy',
        DATE '1994-03-12',
        'MALE',
        'demo.legacy.patient@clinic.local'
    )
ON CONFLICT (id) DO UPDATE
SET first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    date_of_birth = EXCLUDED.date_of_birth,
    gender = EXCLUDED.gender,
    contact_information = EXCLUDED.contact_information;

INSERT INTO medicalrecords (id, patient_id, record_date, diagnosis, treatment, notes)
VALUES
    (
        '9f222222-1111-4111-8111-111111111111',
        '9f111111-1111-4111-8111-111111111111',
        DATE '2026-05-18',
        'Viêm họng cấp',
        'Điều trị ngoại trú, súc họng nước muối sinh lý, theo dõi sốt.',
        'Bệnh nhân đáp ứng tốt, không có biến chứng hô hấp dưới.'
    ),
    (
        '9f222222-2222-4222-8222-222222222222',
        '9f111111-1111-4111-8111-111111111111',
        DATE '2026-06-30',
        'Đau dạ dày chức năng',
        'Điều chỉnh chế độ ăn, dùng thuốc giảm tiết acid trong 14 ngày.',
        'Khuyến nghị tái khám nếu đau bụng tái phát sau bữa tối hoặc có buồn nôn kéo dài.'
    )
ON CONFLICT (id) DO UPDATE
SET patient_id = EXCLUDED.patient_id,
    record_date = EXCLUDED.record_date,
    diagnosis = EXCLUDED.diagnosis,
    treatment = EXCLUDED.treatment,
    notes = EXCLUDED.notes;

INSERT INTO prescriptions (id, medical_record_id, medication_name, dosage, frequency, duration)
VALUES
    (
        '9f333333-1111-4111-8111-111111111111',
        '9f222222-1111-4111-8111-111111111111',
        'Paracetamol 500mg',
        '1 viên',
        'Khi sốt trên 38.5°C, tối đa 3 lần/ngày',
        '3 ngày'
    ),
    (
        '9f333333-2222-4222-8222-222222222222',
        '9f222222-1111-4111-8111-111111111111',
        'Alpha Choay',
        '2 viên',
        '2 lần/ngày sau ăn',
        '5 ngày'
    ),
    (
        '9f333333-3333-4333-8333-333333333333',
        '9f222222-2222-4222-8222-222222222222',
        'Omeprazole 20mg',
        '1 viên',
        '1 lần/ngày trước ăn sáng',
        '14 ngày'
    ),
    (
        '9f333333-4444-4444-8444-444444444444',
        '9f222222-2222-4222-8222-222222222222',
        'Antacid gel',
        '1 gói',
        '3 lần/ngày sau ăn',
        '7 ngày'
    )
ON CONFLICT (id) DO UPDATE
SET medical_record_id = EXCLUDED.medical_record_id,
    medication_name = EXCLUDED.medication_name,
    dosage = EXCLUDED.dosage,
    frequency = EXCLUDED.frequency,
    duration = EXCLUDED.duration;
