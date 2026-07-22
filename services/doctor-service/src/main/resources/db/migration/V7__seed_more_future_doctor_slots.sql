WITH candidate_slots AS (
    SELECT
        d.id AS doctor_id,
        work_days.work_date::date + slot_times.start_at AS start_time,
        work_days.work_date::date + slot_times.start_at + INTERVAL '30 minutes' AS end_time,
        md5(d.id::text || '|' || (work_days.work_date::date + slot_times.start_at)::text) AS slot_hash
    FROM doctors d
    CROSS JOIN generate_series(
        DATE '2026-07-21',
        DATE '2026-08-31',
        INTERVAL '1 day'
    ) AS work_days(work_date)
    CROSS JOIN (
        VALUES
            (TIME '08:00:00'),
            (TIME '08:30:00'),
            (TIME '09:00:00'),
            (TIME '09:30:00'),
            (TIME '10:00:00'),
            (TIME '10:30:00'),
            (TIME '13:30:00'),
            (TIME '14:00:00'),
            (TIME '14:30:00'),
            (TIME '15:00:00'),
            (TIME '15:30:00'),
            (TIME '16:00:00')
    ) AS slot_times(start_at)
    WHERE d.is_active = TRUE
      AND EXTRACT(ISODOW FROM work_days.work_date) BETWEEN 1 AND 5
)
INSERT INTO doctor_slots (id, doctor_id, start_time, end_time, status)
SELECT
    (
        substring(slot_hash, 1, 8) || '-' ||
        substring(slot_hash, 9, 4) || '-' ||
        substring(slot_hash, 13, 4) || '-' ||
        substring(slot_hash, 17, 4) || '-' ||
        substring(slot_hash, 21, 12)
    )::uuid,
    doctor_id,
    start_time,
    end_time,
    'AVAILABLE'
FROM candidate_slots candidate
WHERE NOT EXISTS (
    SELECT 1
    FROM doctor_slots existing
    WHERE existing.doctor_id = candidate.doctor_id
      AND existing.start_time = candidate.start_time
      AND existing.end_time = candidate.end_time
)
ON CONFLICT (id) DO NOTHING;
