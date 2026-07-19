INSERT INTO doctor_slots (id, doctor_id, start_time, end_time, is_booked)
SELECT slots.id, slots.doctor_id, slots.start_time, slots.end_time, FALSE
FROM (
    VALUES
        ('d1000000-0000-0000-0000-000000000001'::uuid, '0e7048a6-e6df-4391-aa38-8ab456dae678'::uuid, '2026-07-08 08:00:00'::timestamp, '2026-07-08 08:30:00'::timestamp),
        ('d1000000-0000-0000-0000-000000000002'::uuid, '0e7048a6-e6df-4391-aa38-8ab456dae678'::uuid, '2026-07-08 09:00:00'::timestamp, '2026-07-08 09:30:00'::timestamp),
        ('d1000000-0000-0000-0000-000000000003'::uuid, '0e7048a6-e6df-4391-aa38-8ab456dae678'::uuid, '2026-07-09 14:00:00'::timestamp, '2026-07-09 14:30:00'::timestamp),

        ('d1000000-0000-0000-0000-000000000011'::uuid, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'::uuid, '2026-07-08 08:30:00'::timestamp, '2026-07-08 09:00:00'::timestamp),
        ('d1000000-0000-0000-0000-000000000012'::uuid, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'::uuid, '2026-07-09 10:00:00'::timestamp, '2026-07-09 10:30:00'::timestamp),
        ('d1000000-0000-0000-0000-000000000013'::uuid, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'::uuid, '2026-07-10 15:00:00'::timestamp, '2026-07-10 15:30:00'::timestamp),

        ('d1000000-0000-0000-0000-000000000021'::uuid, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12'::uuid, '2026-07-08 09:30:00'::timestamp, '2026-07-08 10:00:00'::timestamp),
        ('d1000000-0000-0000-0000-000000000022'::uuid, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12'::uuid, '2026-07-09 13:30:00'::timestamp, '2026-07-09 14:00:00'::timestamp),
        ('d1000000-0000-0000-0000-000000000023'::uuid, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12'::uuid, '2026-07-10 16:00:00'::timestamp, '2026-07-10 16:30:00'::timestamp),

        ('d1000000-0000-0000-0000-000000000031'::uuid, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13'::uuid, '2026-07-08 10:30:00'::timestamp, '2026-07-08 11:00:00'::timestamp),
        ('d1000000-0000-0000-0000-000000000032'::uuid, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13'::uuid, '2026-07-09 15:30:00'::timestamp, '2026-07-09 16:00:00'::timestamp),
        ('d1000000-0000-0000-0000-000000000033'::uuid, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13'::uuid, '2026-07-10 08:00:00'::timestamp, '2026-07-10 08:30:00'::timestamp)
) AS slots(id, doctor_id, start_time, end_time)
JOIN doctors d ON d.id = slots.doctor_id
ON CONFLICT (id) DO NOTHING;
