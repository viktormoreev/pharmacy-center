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

-- Align displayed doctor name with Keycloak demo account
UPDATE doctor
SET name = 'Dr. John Smith'
WHERE license_number = 'UIN-12345';

-- ========================================
-- CUSTOMERS/PATIENTS (with new fields: EGN, primary_doctor_id, insurance_paid_until)
-- ========================================
INSERT INTO customers (name, egn, email, phone, address, date_of_birth, allergies, medical_history, insurance_number, active, insurance_paid_until, primary_doctor_id)
SELECT 
    'John Smith', 
    '7905154321',
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

INSERT INTO customers (name, egn, email, phone, address, date_of_birth, allergies, medical_history, insurance_number, active, insurance_paid_until, primary_doctor_id)
SELECT 
    'Mary Johnson', 
    '9208227654',
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

INSERT INTO customers (name, egn, email, phone, address, date_of_birth, allergies, medical_history, insurance_number, active, insurance_paid_until, primary_doctor_id)
SELECT 
    'Robert Davis', 
    '5711309876',
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

INSERT INTO customers (name, egn, email, phone, address, date_of_birth, allergies, medical_history, insurance_number, active, insurance_paid_until, primary_doctor_id)
SELECT 
    'Emma Wilson', 
    '9603102345',
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

INSERT INTO customers (name, egn, email, phone, address, date_of_birth, allergies, medical_history, insurance_number, active, insurance_paid_until, primary_doctor_id)
SELECT 
    'Peter Georgiev', 
    '8505156789',
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
SET email = 'customer@pharmacy.com',
    name = 'Bob Customer'
WHERE egn = '7905154321';

-- ========================================
-- RECIPES/PRESCRIPTIONS (with sick leave data)
-- ========================================

-- Recipe 1: John Smith - Hypertension (No sick leave)
INSERT INTO recipe (creation_date, doctor_id, customer_id, status, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT 
    '2026-01-10',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1),
    'ACTIVE',
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
INSERT INTO recipe (creation_date, doctor_id, customer_id, status, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT 
    '2026-01-12',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1),
    'ACTIVE',
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
INSERT INTO recipe (creation_date, doctor_id, customer_id, status, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT 
    '2026-01-15',
    (SELECT id FROM doctor WHERE license_number = 'UIN-54321' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '5711309876' LIMIT 1),
    'ACTIVE',
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
INSERT INTO recipe (creation_date, doctor_id, customer_id, status, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT 
    '2026-01-18',
    (SELECT id FROM doctor WHERE license_number = 'UIN-22222' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '9603102345' LIMIT 1),
    'ACTIVE',
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
INSERT INTO recipe (creation_date, doctor_id, customer_id, status, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT 
    '2026-01-20',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '8505156789' LIMIT 1),
    'ACTIVE',
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

-- Recipe 6: John Smith - Follow-up examination (No sick leave)
INSERT INTO recipe (creation_date, doctor_id, customer_id, status, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT
    '2026-02-05',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1),
    'ACTIVE',
    'Blood pressure improved. Continue treatment and low-salt diet.',
    '2026-08-05',
    false,
    NULL,
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM recipe r
    WHERE r.customer_id = (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1)
    AND r.creation_date = '2026-02-05'
);

-- Recipe 7: Mary Johnson - Follow-up examination (No sick leave)
INSERT INTO recipe (creation_date, doctor_id, customer_id, status, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT
    '2026-02-10',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1),
    'ACTIVE',
    'Symptoms resolved. Return if fever or cough persists.',
    '2026-08-10',
    false,
    NULL,
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM recipe r
    WHERE r.customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1)
    AND r.creation_date = '2026-02-10'
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

-- ========================================
-- ADDITIONAL MVP DEMO DATA (for full feature showcase)
-- ========================================

-- Additional medicines
INSERT INTO medicine (name, age_appropriateness, needs_recipe)
SELECT 'Ventolin', 6, true
WHERE NOT EXISTS (SELECT 1 FROM medicine WHERE name = 'Ventolin');

INSERT INTO medicine (name, age_appropriateness, needs_recipe)
SELECT 'Cetirizine', 6, false
WHERE NOT EXISTS (SELECT 1 FROM medicine WHERE name = 'Cetirizine');

INSERT INTO medicine (name, age_appropriateness, needs_recipe)
SELECT 'Metformin', 18, true
WHERE NOT EXISTS (SELECT 1 FROM medicine WHERE name = 'Metformin');

INSERT INTO medicine (name, age_appropriateness, needs_recipe)
SELECT 'Bisoprolol', 18, true
WHERE NOT EXISTS (SELECT 1 FROM medicine WHERE name = 'Bisoprolol');

INSERT INTO medicine (name, age_appropriateness, needs_recipe)
SELECT 'Omeprazole', 16, false
WHERE NOT EXISTS (SELECT 1 FROM medicine WHERE name = 'Omeprazole');

-- Additional customers
INSERT INTO customers (name, egn, email, phone, address, date_of_birth, allergies, medical_history, insurance_number, active, insurance_paid_until, primary_doctor_id)
SELECT
    'Nikolai Petrov',
    '8802145678',
    'n.petrov@email.com',
    '+359888666777',
    '12 Vitosha Blvd, Sofia',
    '1988-02-14',
    'Sulfonamides',
    'Type 2 Diabetes',
    'INS-001239',
    true,
    '2026-12-01',
    (SELECT id FROM doctor WHERE license_number = 'UIN-54321' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE egn = '8802145678');

INSERT INTO customers (name, egn, email, phone, address, date_of_birth, allergies, medical_history, insurance_number, active, insurance_paid_until, primary_doctor_id)
SELECT
    'Silvia Todorova',
    '9409073456',
    'silvia.todorova@email.com',
    '+359888777888',
    '27 Rakovski St, Plovdiv',
    '1994-09-07',
    NULL,
    'Seasonal allergy',
    'INS-001240',
    true,
    '2026-11-15',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE egn = '9409073456');

INSERT INTO customers (name, egn, email, phone, address, date_of_birth, allergies, medical_history, insurance_number, active, insurance_paid_until, primary_doctor_id)
SELECT
    'Georgi Ivanov',
    '7601011122',
    'georgi.ivanov@email.com',
    '+359888999000',
    '8 Tsar Simeon, Varna',
    '1976-01-01',
    'Aspirin',
    'Hypertension',
    'INS-001241',
    true,
    '2025-09-01',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM customers WHERE egn = '7601011122');

-- Additional recipes across months and statuses
INSERT INTO recipe (creation_date, doctor_id, customer_id, status, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT
    '2026-02-14',
    (SELECT id FROM doctor WHERE license_number = 'UIN-54321' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '8802145678' LIMIT 1),
    'FULFILLED',
    'Glycemic control improved, continue treatment.',
    '2026-08-14',
    false,
    NULL,
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM recipe r
    WHERE r.customer_id = (SELECT id FROM customers WHERE egn = '8802145678' LIMIT 1)
      AND r.creation_date = '2026-02-14'
);

INSERT INTO recipe (creation_date, doctor_id, customer_id, status, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT
    '2026-03-03',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '9409073456' LIMIT 1),
    'ACTIVE',
    'Start antihistamine for 14 days.',
    '2026-05-03',
    false,
    NULL,
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM recipe r
    WHERE r.customer_id = (SELECT id FROM customers WHERE egn = '9409073456' LIMIT 1)
      AND r.creation_date = '2026-03-03'
);

INSERT INTO recipe (creation_date, doctor_id, customer_id, status, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT
    '2026-03-10',
    (SELECT id FROM doctor WHERE license_number = 'UIN-33333' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '7601011122' LIMIT 1),
    'EXPIRED',
    'Symptomatic treatment, neurology follow-up if persistent.',
    '2026-04-10',
    true,
    3,
    '2026-03-10'
WHERE NOT EXISTS (
    SELECT 1 FROM recipe r
    WHERE r.customer_id = (SELECT id FROM customers WHERE egn = '7601011122' LIMIT 1)
      AND r.creation_date = '2026-03-10'
);

INSERT INTO recipe (creation_date, doctor_id, customer_id, status, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT
    '2026-04-12',
    (SELECT id FROM doctor WHERE license_number = 'UIN-22222' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '9603102345' LIMIT 1),
    'CANCELLED',
    'Patient did not start treatment, prescription cancelled.',
    '2026-06-12',
    false,
    NULL,
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM recipe r
    WHERE r.customer_id = (SELECT id FROM customers WHERE egn = '9603102345' LIMIT 1)
      AND r.creation_date = '2026-04-12'
);

INSERT INTO recipe (creation_date, doctor_id, customer_id, status, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT
    '2026-05-05',
    (SELECT id FROM doctor WHERE license_number = 'UIN-12345' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1),
    'FULFILLED',
    'Medication adjusted. Return after 2 months.',
    '2026-11-05',
    false,
    NULL,
    NULL
WHERE NOT EXISTS (
    SELECT 1 FROM recipe r
    WHERE r.customer_id = (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1)
      AND r.creation_date = '2026-05-05'
);

INSERT INTO recipe (creation_date, doctor_id, customer_id, status, notes, expiration_date, sick_leave, sick_leave_days, sick_leave_start_date)
SELECT
    '2026-06-01',
    (SELECT id FROM doctor WHERE license_number = 'UIN-11111' LIMIT 1),
    (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1),
    'ACTIVE',
    'Antibiotic course and home rest.',
    '2026-07-01',
    true,
    6,
    '2026-06-01'
WHERE NOT EXISTS (
    SELECT 1 FROM recipe r
    WHERE r.customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1)
      AND r.creation_date = '2026-06-01'
);

-- Additional recipe medicines
INSERT INTO recipe_medicines (recipe_id, medicine_id, dosage, duration_days, instructions, quantity)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '8802145678' LIMIT 1) AND creation_date = '2026-02-14' LIMIT 1),
    (SELECT id FROM medicine WHERE name = 'Metformin' LIMIT 1),
    '500mg twice daily',
    60,
    'Take after meals.',
    120
WHERE NOT EXISTS (
    SELECT 1 FROM recipe_medicines rm
    WHERE rm.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '8802145678' LIMIT 1) AND creation_date = '2026-02-14' LIMIT 1)
      AND rm.medicine_id = (SELECT id FROM medicine WHERE name = 'Metformin' LIMIT 1)
);

INSERT INTO recipe_medicines (recipe_id, medicine_id, dosage, duration_days, instructions, quantity)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9409073456' LIMIT 1) AND creation_date = '2026-03-03' LIMIT 1),
    (SELECT id FROM medicine WHERE name = 'Cetirizine' LIMIT 1),
    '10mg once daily',
    14,
    'Take in the evening.',
    14
WHERE NOT EXISTS (
    SELECT 1 FROM recipe_medicines rm
    WHERE rm.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9409073456' LIMIT 1) AND creation_date = '2026-03-03' LIMIT 1)
      AND rm.medicine_id = (SELECT id FROM medicine WHERE name = 'Cetirizine' LIMIT 1)
);

INSERT INTO recipe_medicines (recipe_id, medicine_id, dosage, duration_days, instructions, quantity)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7601011122' LIMIT 1) AND creation_date = '2026-03-10' LIMIT 1),
    (SELECT id FROM medicine WHERE name = 'Analgin' LIMIT 1),
    '1 tablet when needed',
    7,
    'Max 3 tablets/day.',
    20
WHERE NOT EXISTS (
    SELECT 1 FROM recipe_medicines rm
    WHERE rm.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7601011122' LIMIT 1) AND creation_date = '2026-03-10' LIMIT 1)
      AND rm.medicine_id = (SELECT id FROM medicine WHERE name = 'Analgin' LIMIT 1)
);

INSERT INTO recipe_medicines (recipe_id, medicine_id, dosage, duration_days, instructions, quantity)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1) AND creation_date = '2026-05-05' LIMIT 1),
    (SELECT id FROM medicine WHERE name = 'Bisoprolol' LIMIT 1),
    '5mg once daily',
    60,
    'Measure blood pressure every morning.',
    60
WHERE NOT EXISTS (
    SELECT 1 FROM recipe_medicines rm
    WHERE rm.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1) AND creation_date = '2026-05-05' LIMIT 1)
      AND rm.medicine_id = (SELECT id FROM medicine WHERE name = 'Bisoprolol' LIMIT 1)
);

INSERT INTO recipe_medicines (recipe_id, medicine_id, dosage, duration_days, instructions, quantity)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-06-01' LIMIT 1),
    (SELECT id FROM medicine WHERE name = 'Amoxicillin' LIMIT 1),
    '500mg three times daily',
    6,
    'Finish full antibiotic course.',
    18
WHERE NOT EXISTS (
    SELECT 1 FROM recipe_medicines rm
    WHERE rm.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-06-01' LIMIT 1)
      AND rm.medicine_id = (SELECT id FROM medicine WHERE name = 'Amoxicillin' LIMIT 1)
);

-- Diagnoses table data (for diagnosis module demo)
INSERT INTO diagnoses (recipe_id, icd10_code, name, description, diagnosis_date, is_primary, severity, notes)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1) AND creation_date = '2026-01-10' LIMIT 1),
    'I10',
    'Essential (primary) hypertension',
    'Stable blood pressure values',
    '2026-01-10',
    true,
    'MODERATE',
    'Continue antihypertensive therapy'
WHERE NOT EXISTS (
    SELECT 1 FROM diagnoses d
    WHERE d.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1) AND creation_date = '2026-01-10' LIMIT 1)
      AND d.icd10_code = 'I10'
);

INSERT INTO diagnoses (recipe_id, icd10_code, name, description, diagnosis_date, is_primary, severity, notes)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-06-01' LIMIT 1),
    'J20',
    'Acute bronchitis',
    'Cough, fever, chest discomfort',
    '2026-06-01',
    true,
    'MODERATE',
    'Home rest and antibiotics'
WHERE NOT EXISTS (
    SELECT 1 FROM diagnoses d
    WHERE d.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-06-01' LIMIT 1)
      AND d.icd10_code = 'J20'
);

INSERT INTO diagnoses (recipe_id, icd10_code, name, description, diagnosis_date, is_primary, severity, notes)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9409073456' LIMIT 1) AND creation_date = '2026-03-03' LIMIT 1),
    'J30',
    'Allergic rhinitis',
    'Seasonal allergy symptoms',
    '2026-03-03',
    true,
    'MILD',
    'Monitor during pollen season'
WHERE NOT EXISTS (
    SELECT 1 FROM diagnoses d
    WHERE d.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9409073456' LIMIT 1) AND creation_date = '2026-03-03' LIMIT 1)
      AND d.icd10_code = 'J30'
);

INSERT INTO diagnoses (recipe_id, icd10_code, name, description, diagnosis_date, is_primary, severity, notes)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-01-12' LIMIT 1),
    'J00',
    'Acute nasopharyngitis (common cold)',
    'Upper respiratory tract infection with mild fever',
    '2026-01-12',
    true,
    'MILD',
    'Rest and hydration recommended'
WHERE NOT EXISTS (
    SELECT 1 FROM diagnoses d
    WHERE d.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-01-12' LIMIT 1)
      AND d.icd10_code = 'J00'
);

INSERT INTO diagnoses (recipe_id, icd10_code, name, description, diagnosis_date, is_primary, severity, notes)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '5711309876' LIMIT 1) AND creation_date = '2026-01-15' LIMIT 1),
    'M17.9',
    'Osteoarthritis of knee, unspecified',
    'Chronic joint pain and reduced mobility',
    '2026-01-15',
    true,
    'MODERATE',
    'Pain control and physical therapy advised'
WHERE NOT EXISTS (
    SELECT 1 FROM diagnoses d
    WHERE d.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '5711309876' LIMIT 1) AND creation_date = '2026-01-15' LIMIT 1)
      AND d.icd10_code = 'M17.9'
);

INSERT INTO diagnoses (recipe_id, icd10_code, name, description, diagnosis_date, is_primary, severity, notes)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9603102345' LIMIT 1) AND creation_date = '2026-01-18' LIMIT 1),
    'L23.9',
    'Allergic contact dermatitis, unspecified cause',
    'Skin inflammation with itching and irritation',
    '2026-01-18',
    true,
    'MILD',
    'Avoid allergen exposure and use topical treatment'
WHERE NOT EXISTS (
    SELECT 1 FROM diagnoses d
    WHERE d.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9603102345' LIMIT 1) AND creation_date = '2026-01-18' LIMIT 1)
      AND d.icd10_code = 'L23.9'
);

INSERT INTO diagnoses (recipe_id, icd10_code, name, description, diagnosis_date, is_primary, severity, notes)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '8505156789' LIMIT 1) AND creation_date = '2026-01-20' LIMIT 1),
    'J45.901',
    'Unspecified asthma with acute exacerbation',
    'Wheezing and shortness of breath',
    '2026-01-20',
    true,
    'SEVERE',
    'Short-term sick leave and inhalation therapy'
WHERE NOT EXISTS (
    SELECT 1 FROM diagnoses d
    WHERE d.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '8505156789' LIMIT 1) AND creation_date = '2026-01-20' LIMIT 1)
      AND d.icd10_code = 'J45.901'
);

INSERT INTO diagnoses (recipe_id, icd10_code, name, description, diagnosis_date, is_primary, severity, notes)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1) AND creation_date = '2026-02-05' LIMIT 1),
    'Z09',
    'Follow-up examination after treatment',
    'Control visit with improved blood pressure',
    '2026-02-05',
    true,
    'MILD',
    'Continue current antihypertensive plan'
WHERE NOT EXISTS (
    SELECT 1 FROM diagnoses d
    WHERE d.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1) AND creation_date = '2026-02-05' LIMIT 1)
      AND d.icd10_code = 'Z09'
);

INSERT INTO diagnoses (recipe_id, icd10_code, name, description, diagnosis_date, is_primary, severity, notes)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-02-10' LIMIT 1),
    'Z09.8',
    'Follow-up examination, other specified',
    'Post-viral recovery follow-up',
    '2026-02-10',
    true,
    'MILD',
    'No active symptoms at this visit'
WHERE NOT EXISTS (
    SELECT 1 FROM diagnoses d
    WHERE d.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-02-10' LIMIT 1)
      AND d.icd10_code = 'Z09.8'
);

INSERT INTO diagnoses (recipe_id, icd10_code, name, description, diagnosis_date, is_primary, severity, notes)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '8802145678' LIMIT 1) AND creation_date = '2026-02-14' LIMIT 1),
    'E11.9',
    'Type 2 diabetes mellitus without complications',
    'Routine follow-up for glycemic control',
    '2026-02-14',
    true,
    'MODERATE',
    'Continue metformin and dietary control'
WHERE NOT EXISTS (
    SELECT 1 FROM diagnoses d
    WHERE d.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '8802145678' LIMIT 1) AND creation_date = '2026-02-14' LIMIT 1)
      AND d.icd10_code = 'E11.9'
);

INSERT INTO diagnoses (recipe_id, icd10_code, name, description, diagnosis_date, is_primary, severity, notes)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7601011122' LIMIT 1) AND creation_date = '2026-03-10' LIMIT 1),
    'G43.9',
    'Migraine, unspecified',
    'Acute headache episode with reduced functionality',
    '2026-03-10',
    true,
    'MODERATE',
    'Symptomatic treatment and observation'
WHERE NOT EXISTS (
    SELECT 1 FROM diagnoses d
    WHERE d.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7601011122' LIMIT 1) AND creation_date = '2026-03-10' LIMIT 1)
      AND d.icd10_code = 'G43.9'
);

INSERT INTO diagnoses (recipe_id, icd10_code, name, description, diagnosis_date, is_primary, severity, notes)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9603102345' LIMIT 1) AND creation_date = '2026-04-12' LIMIT 1),
    'L20.9',
    'Atopic dermatitis, unspecified',
    'Recurrent skin inflammation flare',
    '2026-04-12',
    true,
    'MILD',
    'Prescription cancelled after patient decision'
WHERE NOT EXISTS (
    SELECT 1 FROM diagnoses d
    WHERE d.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9603102345' LIMIT 1) AND creation_date = '2026-04-12' LIMIT 1)
      AND d.icd10_code = 'L20.9'
);

INSERT INTO diagnoses (recipe_id, icd10_code, name, description, diagnosis_date, is_primary, severity, notes)
SELECT
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1) AND creation_date = '2026-05-05' LIMIT 1),
    'I10',
    'Essential (primary) hypertension',
    'Control visit for chronic hypertension',
    '2026-05-05',
    true,
    'MODERATE',
    'Medication adjusted and follow-up scheduled'
WHERE NOT EXISTS (
    SELECT 1 FROM diagnoses d
    WHERE d.recipe_id = (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7905154321' LIMIT 1) AND creation_date = '2026-05-05' LIMIT 1)
      AND d.icd10_code = 'I10'
);

-- Sick leaves table data (for sick leave module demo)
INSERT INTO sick_leaves (leave_number, recipe_id, start_date, duration_days, end_date, reason, status, issue_date, notes)
SELECT
    'SL-20260112-A1B2',
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-01-12' LIMIT 1),
    '2026-01-12',
    5,
    '2026-01-16',
    'Acute viral infection recovery',
    'COMPLETED',
    '2026-01-12',
    'Recovered without complications'
WHERE NOT EXISTS (SELECT 1 FROM sick_leaves WHERE leave_number = 'SL-20260112-A1B2');

INSERT INTO sick_leaves (leave_number, recipe_id, start_date, duration_days, end_date, reason, status, issue_date, notes)
SELECT
    'SL-20260120-C3D4',
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '8505156789' LIMIT 1) AND creation_date = '2026-01-20' LIMIT 1),
    '2026-01-20',
    7,
    '2026-01-26',
    'Asthma exacerbation',
    'COMPLETED',
    '2026-01-20',
    'Follow-up after completion'
WHERE NOT EXISTS (SELECT 1 FROM sick_leaves WHERE leave_number = 'SL-20260120-C3D4');

INSERT INTO sick_leaves (leave_number, recipe_id, start_date, duration_days, end_date, reason, status, issue_date, notes)
SELECT
    'SL-20260310-E5F6',
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '7601011122' LIMIT 1) AND creation_date = '2026-03-10' LIMIT 1),
    '2026-03-10',
    3,
    '2026-03-12',
    'Severe migraine episode',
    'CANCELLED',
    '2026-03-10',
    'Cancelled due to early symptom resolution'
WHERE NOT EXISTS (SELECT 1 FROM sick_leaves WHERE leave_number = 'SL-20260310-E5F6');

INSERT INTO sick_leaves (leave_number, recipe_id, start_date, duration_days, end_date, reason, status, issue_date, notes)
SELECT
    'SL-20260601-G7H8',
    (SELECT id FROM recipe WHERE customer_id = (SELECT id FROM customers WHERE egn = '9208227654' LIMIT 1) AND creation_date = '2026-06-01' LIMIT 1),
    '2026-06-01',
    6,
    '2026-06-06',
    'Acute bronchitis recovery',
    'ACTIVE',
    '2026-06-01',
    'Avoid physical exertion'
WHERE NOT EXISTS (SELECT 1 FROM sick_leaves WHERE leave_number = 'SL-20260601-G7H8');
