-- Seed doctor accounts into users table matching doctor-service user_ids
INSERT INTO users (id, email, full_name, phone_number, password_hash, status)
VALUES 
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'dr.nva@clinic.com', 'Dr. Nguyen Van A', '0912345678', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeg6Lruj3vjPGga31lW', 'ACTIVE'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'dr.ttb@clinic.com', 'Dr. Tran Thi B', '0987654321', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeg6Lruj3vjPGga31lW', 'ACTIVE'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'dr.lvc@clinic.com', 'Dr. Le Van C', '0901234567', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeg6Lruj3vjPGga31lW', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

-- Assign DOCTOR role to these users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE r.name = 'DOCTOR'
  AND u.email IN ('dr.nva@clinic.com', 'dr.ttb@clinic.com', 'dr.lvc@clinic.com')
ON CONFLICT (user_id, role_id) DO NOTHING;
