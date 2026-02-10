-- ========================================
-- MEDICAL RECORD SYSTEM - SAMPLE DATA
-- Updated: January 26, 2026
-- Includes: Doctor specialties, Patient EGN, Primary doctors, Sick leaves
-- ========================================

-- ========================================
-- MEDICINES
-- ========================================
INSERT INTO medicine (name, age_appropriateness, needs_recipe) 
SELECT 'Analgin', 18, false 
WHERE NOT EXISTS (SELECT 1 FROM medicine WHERE name = 'Analgin');

INSERT INTO medicine (name, age_appropriateness, needs_recipe) 
SELECT 'Paracetamol', 12, false 
WHERE NOT EXISTS (SELECT 1 FROM medicine WHERE name = 'Paracetamol');

INSERT INTO medicine (name, age_appropriateness, needs_recipe) 
SELECT 'Nurofen', 16, false 
WHERE NOT EXISTS (SELECT 1 FROM medicine WHERE name = 'Nurofen');

INSERT INTO medicine (name, age_appropriateness, needs_recipe) 
SELECT 'Aspirin', 18, false 
WHERE NOT EXISTS (SELECT 1 FROM medicine WHERE name = 'Aspirin');

INSERT INTO medicine (name, age_appropriateness, needs_recipe) 
SELECT 'Amoxicillin', 12, true 
WHERE NOT EXISTS (SELECT 1 FROM medicine WHERE name = 'Amoxicillin');

-- ========================================
-- DOCTORS (with new fields)
-- ========================================
INSERT INTO doctor (name, license_number, specialty, is_primary_doctor, email, phone_number)
SELECT 'Dr. James Wilson', 'UIN-12345', 'General Practitioner', true, 'dr.wilson@hospital.bg', '+359888123456'
WHERE NOT EXISTS (SELECT 1 FROM doctor WHERE license_number = 'UIN-12345');

INSERT INTO doctor (name, license_number, specialty, is_primary_doctor, email, phone_number)
SELECT 'Dr. Sarah Mitchell', 'UIN-54321', 'Cardiologist', true, 'dr.mitchell@cardio.bg', '+359888234567'
WHERE NOT EXISTS (SELECT 1 FROM doctor WHERE license_number = 'UIN-54321');

INSERT INTO doctor (name, license_number, specialty, is_primary_doctor, email, phone_number)
SELECT 'Dr. Michael Brown', 'UIN-11111', 'Pediatrician', true, 'dr.brown@pediatrics.bg', '+359888345678'
WHERE NOT EXISTS (SELECT 1 FROM doctor WHERE license_number = 'UIN-11111');

INSERT INTO doctor (name, license_number, specialty, is_primary_doctor, email, phone_number)
SELECT 'Dr. Elena Ivanova', 'UIN-22222', 'Dermatologist', false, 'dr.ivanova@derma.bg', '+359888456789'
WHERE NOT EXISTS (SELECT 1 FROM doctor WHERE license_number = 'UIN-22222');

INSERT INTO doctor (name, license_number, specialty, is_primary_doctor, email, phone_number)
SELECT 'Dr. Stefan Nikolov', 'UIN-33333', 'Neurologist', false, 's.nikolov@neuro.bg', '+359888567890'
WHERE NOT EXISTS (SELECT 1 FROM doctor WHERE license_number = 'UIN-33333');

-- Keycloak account mapping for role DOCTOR (must match Keycloak email claim)
UPDATE doctor
SET email = 'doctor@pharmacy.com'
WHERE license_number = 'UIN-12345';

-- ========================================
-- CUSTOMERS/PATIENTS (with new fields: EGN, primary_doctor_id, insurance_paid_until)
-- ========================================
INSERT INTO customers (name, egn, age, email, phone, address, date_of_birth, allergies, medical_history, insurance_number, active, insurance_paid_until, primary_doctor_id)
SELECT 
    'John Smith', 
    '7905154321', 
    45, 
    'john.smith@email.com', 
    '+359888111222', 
    '123 Main St, Sofia', 
    '1979-05-15', 
    'Penicillin, Aspirin', 
    'Hypertension, Diabetes Type 2', 
    'INS-001234', 
    true,
    '2026-08-01',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE egn = '7905154321');

INSERT INTO customers (name, egn, age, email, phone, address, date_of_birth, allergies, medical_history, insurance_number, active, insurance_paid_until, primary_doctor_id)
SELECT 
    'Mary Johnson', 
    '9208227654', 
    32, 
    'mary.johnson@email.com', 
    '+359888222333', 
    '456 Oak Ave, Plovdiv', 
    '1992-08-22', 
    'Peanuts', 
    'None', 
    'INS-001235', 
    true,
    '2026-07-15',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE egn = '9208227654');

INSERT INTO customers (name, egn, age, email, phone, address, date_of_birth, allergies, medical_history, insurance_number, active, insurance_paid_until, primary_doctor_id)
SELECT 
    'Robert Davis', 
    '5711309876', 
    67, 
    'robert.davis@email.com', 
    '+359888333444', 
    '789 Pine Rd, Varna', 
    '1957-11-30', 
    NULL, 
    'Arthritis, High Cholesterol', 
    'INS-001236', 
    true,
    '2026-09-01',
    (SELECT id FROM doctor WHERE license_number = 'UIN-54321' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE egn = '5711309876');

INSERT INTO customers (name, egn, age, email, phone, address, date_of_birth, allergies, medical_history, insurance_number, active, insurance_paid_until, primary_doctor_id)
SELECT 
    'Emma Wilson', 
    '9603102345', 
    28, 
    'emma.wilson@email.com', 
    '+359888444555', 
    '321 Elm St, Burgas', 
    '1996-03-10', 
    'Latex', 
    NULL, 
    'INS-001237', 
    true,
    '2026-06-30',
    (SELECT id FROM doctor WHERE license_number = 'UIN-11111' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE egn = '9603102345');

INSERT INTO customers (name, egn, age, email, phone, address, date_of_birth, allergies, medical_history, insurance_number, active, insurance_paid_until, primary_doctor_id)
SELECT 
    'Peter Georgiev', 
    '8505156789', 
    39, 
    'p.georgiev@email.com', 
    '+359888555666', 
    '555 River St, Ruse', 
    '1985-05-15', 
    NULL, 
    'Asthma', 
    'INS-001238', 
    true,
    '2026-07-20',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE egn = '8505156789');

-- Keycloak account mapping for role CUSTOMER (for demo/testing login)
UPDATE customers
SET email = 'customer@pharmacy.com'
WHERE egn = '7905154321';

-- ========================================
-- RECIPES/PRESCRIPTIONS (with sick leave data)
-- ========================================

-- Recipe 1: John Smith - Hypertension (No sick leave)
INSERT INTO recipe (creation_date, doctor_id, customer_id, status, diagnosis, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT 
    '2026-01-10',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1),
    'ACTIVE',
    'Hypertension management',
    'Monitor blood pressure daily. Return in 30 days for follow-up.',
    '2026-07-10',
    false,
    NULL,
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM recipe r
    WHERE r.customer_id = (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1)
    AND r.creation_date = '2026-01-10'
);

-- Recipe 2: Mary Johnson - Common cold with sick leave
INSERT INTO recipe (creation_date, doctor_id, customer_id, status, diagnosis, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT 
    '2026-01-12',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1),
    'ACTIVE',
    'Acute viral respiratory infection (Common cold)',
    'Rest and plenty of fluids. Complete full course of medication. Sick leave issued for 5 days.',
    '2026-02-12',
    true,
    5,
    '2026-01-12'
WHERE NOT EXISTS (
    SELECT 1 FROM recipe r
    WHERE r.customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1)
    AND r.creation_date = '2026-01-12'
);

-- Recipe 3: Robert Davis - Arthritis (No sick leave)
INSERT INTO recipe (creation_date, doctor_id, customer_id, status, diagnosis, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT 
    '2026-01-15',
    (SELECT id FROM doctor WHERE license_number = 'UIN-54321' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '5711309876' LIMIT 1),
    'ACTIVE',
    'Osteoarthritis - knee joint',
    'Anti-inflammatory medication. Physical therapy recommended.',
    '2026-07-15',
    false,
    NULL,
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM recipe r
    WHERE r.customer_id = (SELECT id FROM customers WHERE egn = '5711309876' LIMIT 1)
    AND r.creation_date = '2026-01-15'
);

-- Recipe 4: Emma Wilson - Skin allergy
INSERT INTO recipe (creation_date, doctor_id, customer_id, status, diagnosis, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT 
    '2026-01-18',
    (SELECT id FROM doctor WHERE license_number = 'UIN-22222' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '9603102345' LIMIT 1),
    'ACTIVE',
    'Allergic dermatitis',
    'Avoid latex products. Apply cream twice daily.',
    '2026-03-18',
    false,
    NULL,
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM recipe r
    WHERE r.customer_id = (SELECT id FROM customers WHERE egn = '9603102345' LIMIT 1)
    AND r.creation_date = '2026-01-18'
);

-- Recipe 5: Peter Georgiev - Asthma exacerbation with sick leave
INSERT INTO recipe (creation_date, doctor_id, customer_id, status, diagnosis, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT 
    '2026-01-20',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '8505156789' LIMIT 1),
    'ACTIVE',
    'Acute asthma exacerbation',
    'Use inhaler as prescribed. Avoid triggers. Sick leave for 7 days.',
    '2026-04-20',
    true,
    7,
    '2026-01-20'
WHERE NOT EXISTS (
    SELECT 1 FROM recipe r
    WHERE r.customer_id = (SELECT id FROM customers WHERE egn = '8505156789' LIMIT 1)
    AND r.creation_date = '2026-01-20'
);

-- ========================================
-- RECIPE MEDICINES (Prescription line items)
-- ========================================

-- For John Smith's Hypertension prescription
INSERT INTO recipe_medicines (recipe_id, medicine_id, dosage, duration_days, instructions, quantity)
SELECT 
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1) AND creation_date = '2026-01-10' LIMIT 1),
    (SELECT id FROM medicine WHERE name = 'Analgin' LIMIT 1),
    '1 tablet twice daily',
    30,
    'Take with food. Do not exceed recommended dose.',
    60
WHERE NOT EXISTS (
    SELECT 1 FROM recipe_medicines rm
    WHERE rm.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1) AND creation_date = '2026-01-10' LIMIT 1)
    AND rm.medicine_id = (SELECT id FROM medicine WHERE name = 'Analgin' LIMIT 1)
);

-- For Mary Johnson's Common cold prescription
INSERT INTO recipe_medicines (recipe_id, medicine_id, dosage, duration_days, instructions, quantity)
SELECT 
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-01-12' LIMIT 1),
    (SELECT id FROM medicine WHERE name = 'Paracetamol' LIMIT 1),
    '2 tablets every 6 hours',
    5,
    'Take with plenty of water. Do not take on empty stomach.',
    40
WHERE NOT EXISTS (
    SELECT 1 FROM recipe_medicines rm
    WHERE rm.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-01-12' LIMIT 1)
    AND rm.medicine_id = (SELECT id FROM medicine WHERE name = 'Paracetamol' LIMIT 1)
);

INSERT INTO recipe_medicines (recipe_id, medicine_id, dosage, duration_days, instructions, quantity)
SELECT 
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-01-12' LIMIT 1),
    (SELECT id FROM medicine WHERE name = 'Nurofen' LIMIT 1),
    '1 tablet three times daily',
    5,
    'Take after meals to reduce stomach irritation.',
    15
WHERE NOT EXISTS (
    SELECT 1 FROM recipe_medicines rm
    WHERE rm.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-01-12' LIMIT 1)
    AND rm.medicine_id = (SELECT id FROM medicine WHERE name = 'Nurofen' LIMIT 1)
);

-- For Robert Davis's Arthritis prescription
INSERT INTO recipe_medicines (recipe_id, medicine_id, dosage, duration_days, instructions, quantity)
SELECT 
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '5711309876' LIMIT 1) AND creation_date = '2026-01-15' LIMIT 1),
    (SELECT id FROM medicine WHERE name = 'Nurofen' LIMIT 1),
    '2 tablets twice daily',
    30,
    'Take with food. Long-term use - monitor for side effects.',
    120
WHERE NOT EXISTS (
    SELECT 1 FROM recipe_medicines rm
    WHERE rm.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '5711309876' LIMIT 1) AND creation_date = '2026-01-15' LIMIT 1)
    AND rm.medicine_id = (SELECT id FROM medicine WHERE name = 'Nurofen' LIMIT 1)
);

-- For Peter Georgiev's Asthma prescription
INSERT INTO recipe_medicines (recipe_id, medicine_id, dosage, duration_days, instructions, quantity)
SELECT 
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '8505156789' LIMIT 1) AND creation_date = '2026-01-20' LIMIT 1),
    (SELECT id FROM medicine WHERE name = 'Amoxicillin' LIMIT 1),
    '500mg three times daily',
    7,
    'Complete full course even if feeling better. Prescription required.',
    21
WHERE NOT EXISTS (
    SELECT 1 FROM recipe_medicines rm
    WHERE rm.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '8505156789' LIMIT 1) AND creation_date = '2026-01-20' LIMIT 1)
    AND rm.medicine_id = (SELECT id FROM medicine WHERE name = 'Amoxicillin' LIMIT 1)
);
