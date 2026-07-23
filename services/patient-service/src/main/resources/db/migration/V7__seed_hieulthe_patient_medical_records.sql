CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
    target_email TEXT := 'hieulthe186408@fpt.edu.vn';
    target_user_id UUID := '6a0e4597-822a-4798-9449-9961f6d2bdc3';
    target_patient_id UUID := '383ef112-f734-4b09-a926-96ee9766bf0c';
    resolved_patient_id UUID;

    record_respiratory_id UUID := '383ef112-f734-4b09-a926-96ee9766b001';
    record_digestive_id UUID := '383ef112-f734-4b09-a926-96ee9766b002';
BEGIN
    SELECT id
    INTO resolved_patient_id
    FROM patients
    WHERE user_id = target_user_id
       OR id = target_patient_id
       OR LOWER(contact_information) = target_email
    ORDER BY CASE
        WHEN user_id = target_user_id THEN 0
        WHEN id = target_patient_id THEN 1
        ELSE 2
    END
    LIMIT 1;

    IF resolved_patient_id IS NULL THEN
        resolved_patient_id := target_patient_id;

        INSERT INTO patients (id, user_id, first_name, last_name, date_of_birth, gender, contact_information)
        VALUES (
            resolved_patient_id,
            target_user_id,
            'Hieu',
            NULL,
            NULL,
            NULL,
            target_email
        );
    ELSE
        UPDATE patients
        SET user_id = CASE
                WHEN user_id IS NULL THEN target_user_id
                ELSE user_id
            END,
            first_name = CASE
                WHEN first_name IS NULL OR first_name = '' THEN 'Hieu'
                ELSE first_name
            END,
            contact_information = CASE
                WHEN contact_information IS NULL OR contact_information = '' THEN target_email
                ELSE contact_information
            END
        WHERE id = resolved_patient_id;
    END IF;

    INSERT INTO medicalrecords (id, patient_id, record_date, diagnosis, treatment, notes)
    VALUES
        (
            record_respiratory_id,
            resolved_patient_id,
            DATE '2026-07-10',
            'Viêm mũi họng cấp',
            'Điều trị ngoại trú, vệ sinh mũi họng bằng nước muối sinh lý, uống thuốc theo đơn và nghỉ ngơi.',
            'Tái khám sau 5-7 ngày nếu còn sốt, ho kéo dài hoặc đau họng tăng.'
        ),
        (
            record_digestive_id,
            resolved_patient_id,
            DATE '2026-07-22',
            'Rối loạn tiêu hóa',
            'Bù nước, ăn thức ăn mềm dễ tiêu, dùng thuốc hỗ trợ tiêu hóa theo đơn.',
            'Theo dõi dấu hiệu mất nước, đi khám ngay nếu đau bụng tăng hoặc tiêu chảy nhiều lần trong ngày.'
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
            '383ef112-f734-4b09-a926-96ee9766b101',
            record_respiratory_id,
            'Paracetamol 500mg',
            '1 viên',
            'Khi sốt trên 38.5°C, tối đa 3 lần/ngày',
            '3 ngày'
        ),
        (
            '383ef112-f734-4b09-a926-96ee9766b102',
            record_respiratory_id,
            'Loratadine 10mg',
            '1 viên',
            '1 lần/ngày vào buổi tối',
            '5 ngày'
        ),
        (
            '383ef112-f734-4b09-a926-96ee9766b103',
            record_respiratory_id,
            'Nước muối sinh lý NaCl 0.9%',
            'Xịt/rửa mũi',
            '3-4 lần/ngày',
            '7 ngày'
        ),
        (
            '383ef112-f734-4b09-a926-96ee9766b201',
            record_digestive_id,
            'Oresol',
            '1 gói pha đúng hướng dẫn',
            'Uống từng ngụm nhỏ sau mỗi lần đi ngoài',
            '2 ngày'
        ),
        (
            '383ef112-f734-4b09-a926-96ee9766b202',
            record_digestive_id,
            'Diosmectite 3g',
            '1 gói',
            '2 lần/ngày sau ăn',
            '3 ngày'
        ),
        (
            '383ef112-f734-4b09-a926-96ee9766b203',
            record_digestive_id,
            'Men vi sinh',
            '1 gói',
            '2 lần/ngày',
            '5 ngày'
        )
    ON CONFLICT (id) DO UPDATE
    SET medical_record_id = EXCLUDED.medical_record_id,
        medication_name = EXCLUDED.medication_name,
        dosage = EXCLUDED.dosage,
        frequency = EXCLUDED.frequency,
        duration = EXCLUDED.duration;
END $$;
