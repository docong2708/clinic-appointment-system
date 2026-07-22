-- Seed doctors
INSERT INTO doctors (id, name, specialization, phone_number, email, is_active, user_id) VALUES 
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Dr. Nguyen Van A', 'Tim mạch', '0912345678', 'dr.nva@clinic.com', TRUE, 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Dr. Tran Thi B', 'Nhi khoa', '0987654321', 'dr.ttb@clinic.com', TRUE, 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Dr. Le Van C', 'Răng Hàm Mặt', '0901234567', 'dr.lvc@clinic.com', TRUE, 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13');

-- Seed doctor slots
-- Slots for Dr. Nguyen Van A (id: a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11)
INSERT INTO doctor_slots (id, doctor_id, start_time, end_time, is_booked) VALUES 
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2026-07-04 08:00:00', '2026-07-04 09:00:00', FALSE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2026-07-04 09:30:00', '2026-07-04 10:30:00', FALSE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2026-07-04 14:00:00', '2026-07-04 15:00:00', FALSE);

-- Slots for Dr. Tran Thi B (id: a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12)
INSERT INTO doctor_slots (id, doctor_id, start_time, end_time, is_booked) VALUES 
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380b21', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '2026-07-04 08:30:00', '2026-07-04 09:30:00', FALSE),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380b22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '2026-07-04 10:00:00', '2026-07-04 11:00:00', FALSE);
