-- MedVault Database Schema - Simplified for Admin Verification
-- PostgreSQL DDL Script

-- Drop existing tables if they exist (in correct order due to foreign keys)
DROP TABLE IF EXISTS appointment CASCADE;
DROP TABLE IF EXISTS otp CASCADE;
DROP TABLE IF EXISTS patient CASCADE;
DROP TABLE IF EXISTS doctor CASCADE;
DROP TABLE IF EXISTS admin CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create users table (main authentication table)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'PATIENT', 'DOCTOR')),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes on users table
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_status ON users(role, status);

-- Create OTP table
CREATE TABLE otp (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'PATIENT', 'DOCTOR')),
    otp_code VARCHAR(5) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT unique_user_role UNIQUE (user_id, role)
);

-- Create patient profile table (SIMPLIFIED FOR VERIFICATION CARD)
CREATE TABLE patient (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    -- Personal Information
    date_of_birth DATE,
    gender VARCHAR(50),
    blood_group VARCHAR(10),
    -- Contact Information
    phone VARCHAR(50),
    -- Address Information
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    pincode VARCHAR(20),
    -- Document Upload
    id_proof_path VARCHAR(500),
    -- Registration Date (auto-set)
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create doctor profile table (SIMPLIFIED FOR VERIFICATION CARD)
CREATE TABLE doctor (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    -- Personal Information
    date_of_birth DATE,
    gender VARCHAR(50),
    profile_photo_path VARCHAR(500),
    -- Professional Information
    medical_registration_number VARCHAR(255) UNIQUE,
    licensing_authority VARCHAR(255),
    specialization VARCHAR(255),
    qualification VARCHAR(500),
    experience INTEGER,
    -- Contact Information
    phone VARCHAR(50),
    -- Clinic/Practice Information
    clinic_hospital_name VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    pincode VARCHAR(20),
    -- Document Uploads
    medical_license_path VARCHAR(500),
    degree_certificates_path VARCHAR(500)
);

-- Create admin profile table
CREATE TABLE admin (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    phone VARCHAR(50),
    designation VARCHAR(255),
    department VARCHAR(255),
    is_super_admin BOOLEAN DEFAULT FALSE
);

-- Create appointment table
CREATE TABLE appointment (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
    doctor_id BIGINT NOT NULL REFERENCES doctor(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    time TIME NOT NULL,
    final_report TEXT,
    status VARCHAR(50) DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED'))
);

-- Create indexes for appointments
CREATE INDEX idx_appointment_patient ON appointment(patient_id);
CREATE INDEX idx_appointment_doctor ON appointment(doctor_id);
CREATE INDEX idx_appointment_date ON appointment(date);
CREATE INDEX idx_appointment_status ON appointment(status);

-- Create indexes for doctor search
CREATE INDEX idx_doctor_specialization ON doctor(specialization);
CREATE INDEX idx_doctor_city ON doctor(city);

-- Insert first admin user (REQUIRED FOR SYSTEM TO WORK)
-- Password: "admin123" (BCrypt hashed)
INSERT INTO users (name, email, password, role, status, created_at, updated_at)
VALUES (
    'System Administrator',
    'admin@medvault.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert admin profile for the first admin
INSERT INTO admin (id, phone, designation, department, is_super_admin)
VALUES (
    (SELECT id FROM users WHERE email = 'admin@medvault.com'),
    '+1234567890',
    'System Administrator',
    'IT Department',
    TRUE
);

-- Verify the setup
SELECT 
    u.id, 
    u.name, 
    u.email, 
    u.role, 
    u.status, 
    a.designation,
    a.is_super_admin
FROM users u
LEFT JOIN admin a ON u.id = a.id
WHERE u.role = 'ADMIN';

-- Sample queries for testing

-- Get all pending users
SELECT u.id, u.name, u.email, u.role, u.status, u.created_at
FROM users u
WHERE u.status = 'PENDING'
ORDER BY u.created_at DESC;

-- Get all active patients with profiles
SELECT 
    u.id, 
    u.name, 
    u.email, 
    p.date_of_birth,
    p.gender, 
    p.blood_group,
    p.phone,
    p.city,
    p.state,
    p.registration_date
FROM users u
JOIN patient p ON u.id = p.id
WHERE u.status = 'ACTIVE'
ORDER BY u.name;

-- Get all active doctors with profiles
SELECT 
    u.id, 
    u.name, 
    u.email, 
    d.medical_registration_number,
    d.specialization,
    d.qualification,
    d.experience, 
    d.phone,
    d.clinic_hospital_name,
    d.city
FROM users u
JOIN doctor d ON u.id = d.id
WHERE u.status = 'ACTIVE'
ORDER BY u.name;

-- Get pending patients with full details for admin verification
SELECT 
    u.id,
    u.name,
    u.email,
    u.created_at,
    p.date_of_birth,
    p.gender,
    p.blood_group,
    p.phone,
    p.address,
    p.city,
    p.state,
    p.country,
    p.pincode,
    p.id_proof_path,
    p.registration_date
FROM users u
JOIN patient p ON u.id = p.id
WHERE u.status = 'PENDING' AND u.role = 'PATIENT'
ORDER BY u.created_at DESC;

-- Get pending doctors with full details for admin verification
SELECT 
    u.id,
    u.name,
    u.email,
    u.created_at,
    d.date_of_birth,
    d.gender,
    d.profile_photo_path,
    d.medical_registration_number,
    d.licensing_authority,
    d.specialization,
    d.qualification,
    d.experience,
    d.phone,
    d.clinic_hospital_name,
    d.city,
    d.state,
    d.country,
    d.pincode,
    d.medical_license_path,
    d.degree_certificates_path
FROM users u
JOIN doctor d ON u.id = d.id
WHERE u.status = 'PENDING' AND u.role = 'DOCTOR'
ORDER BY u.created_at DESC;

-- Get appointments with patient and doctor details
SELECT 
    a.id,
    a.date,
    a.time,
    a.status,
    pu.name AS patient_name,
    p.phone AS patient_phone,
    du.name AS doctor_name,
    d.specialization,
    d.clinic_hospital_name
FROM appointment a
JOIN patient p ON a.patient_id = p.id
JOIN users pu ON p.id = pu.id
JOIN doctor d ON a.doctor_id = d.id
JOIN users du ON d.id = du.id
ORDER BY a.date DESC, a.time DESC;

-- Get admin statistics
SELECT 
    COUNT(*) FILTER (WHERE role = 'PATIENT' AND status = 'ACTIVE') as active_patients,
    COUNT(*) FILTER (WHERE role = 'PATIENT' AND status = 'PENDING') as pending_patients,
    COUNT(*) FILTER (WHERE role = 'DOCTOR' AND status = 'ACTIVE') as active_doctors,
    COUNT(*) FILTER (WHERE role = 'DOCTOR' AND status = 'PENDING') as pending_doctors,
    COUNT(*) FILTER (WHERE role = 'ADMIN' AND status = 'ACTIVE') as active_admins
FROM users;
