package com.medibook.medibook_backend.service;

import com.medibook.medibook_backend.dto.*;
import com.medibook.medibook_backend.entity.*;
import com.medibook.medibook_backend.repository.*;
import com.medibook.medibook_backend.security.JwtService;
import jakarta.transaction.Transactional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminRepository adminRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, OtpRepository otpRepository,
            PatientRepository patientRepository, DoctorRepository doctorRepository,
            AdminRepository adminRepository, EmailService emailService,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.adminRepository = adminRepository;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Generate OTP for registration
     */
    @Transactional
    public Map<String, Object> generateOtp(GenerateOtpRequest request) {
        // Validate role
        User.Role role;
        try {
            role = User.Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role. Must be ADMIN, PATIENT, or DOCTOR");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {

            throw new RuntimeException("Email already in use");
        }

        // Create new user with PENDING status
        User user = new User(request.getName(), request.getEmail(), role);
        user.setStatus(VerificationStatus.PENDING);
        user = userRepository.save(user);

        // Generate 5-digit OTP
        String otpCode = String.format("%05d", new Random().nextInt(90000) + 10000);

        // Delete any existing OTP for this user+role combination
        otpRepository.deleteByUserIdAndRole(user.getId(), role);

        // Create new OTP with emailOrPhone field
        Otp otp = new Otp(user.getId(), request.getEmail(), role, otpCode);
        otp = otpRepository.save(otp);

        // Send OTP email
        emailService.sendOtpEmail(user.getEmail(), user.getName(), otpCode);

        // Return response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP sent to your email");
        response.put("userId", user.getId());
        response.put("role", role.name());

        return response;
    }

    /**
     * Verify OTP
     */
    @Transactional
    public Map<String, Object> verifyOtp(VerifyOtpRequest request) {
        // Validate role
        User.Role role;
        try {
            role = User.Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role");
        }

        // Find OTP record that is not used
        Otp otp = otpRepository.findByUserIdAndRoleAndUsedFalse(request.getUserId(), role)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Please request a new one."));

        // Check if expired (10 minutes)
        if (otp.isExpired()) {

            throw new RuntimeException("Invalid or expired OTP. Please request a new one.");
        }

        // Validate OTP code
        if (!otp.getOtpCode().equals(request.getOtp())) {

            throw new RuntimeException("Invalid or expired OTP. Please request a new one.");
        }

        // Mark OTP as used instead of deleting
        otp.setUsed(true);
        otpRepository.save(otp);

        // Return success response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "OTP verified. Complete your profile.");
        response.put("userId", request.getUserId());
        response.put("role", role.name());

        return response;
    }

    /**
     * Complete Patient Profile
     */
    @Transactional
    public Map<String, Object> completePatientProfile(CompletePatientProfileRequest request) {
        // Find user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify role
        if (user.getRole() != User.Role.PATIENT) {
            throw new RuntimeException("User is not a patient");
        }

        // Hash and set password (don't save yet - let cascade handle it)
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Create or update patient profile
        Patient patient = patientRepository.findById(user.getId()).orElse(null);

        boolean isNewPatient = (patient == null);
        if (isNewPatient) {
            patient = new Patient();
            patient.setUser(user);
            patient.setId(user.getId());
        }

        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());
        patient.setCity(request.getCity());
        patient.setState(request.getState());
        patient.setCountry(request.getCountry());
        patient.setPincode(request.getPincode());
        patient.setIdProofPath(request.getIdProofPath());

        // Save both user and patient
        userRepository.save(user);
        patientRepository.save(patient);

        // Return response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Profile saved. Waiting for admin verification.");

        return response;
    }

    /**
     * Complete Doctor Profile
     */
    @Transactional
    public Map<String, Object> completeDoctorProfile(CompleteDoctorProfileRequest request) {
        // Find user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify role
        if (user.getRole() != User.Role.DOCTOR) {
            throw new RuntimeException("User is not a doctor");
        }

        // Hash and set password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Create or update doctor profile
        Doctor doctor = doctorRepository.findById(user.getId()).orElse(null);

        boolean isNewDoctor = (doctor == null);
        if (isNewDoctor) {
            doctor = new Doctor();
            doctor.setUser(user);
            doctor.setId(user.getId());
        }

        doctor.setDateOfBirth(request.getDateOfBirth());
        doctor.setGender(request.getGender());
        doctor.setProfilePhotoPath(request.getProfilePhotoPath());
        doctor.setMedicalRegistrationNumber(request.getMedicalRegistrationNumber());
        doctor.setLicensingAuthority(request.getLicensingAuthority());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());
        doctor.setExperience(request.getExperience());
        doctor.setPhone(request.getPhone());
        doctor.setClinicHospitalName(request.getClinicHospitalName());
        doctor.setCity(request.getCity());
        doctor.setState(request.getState());
        doctor.setCountry(request.getCountry());
        doctor.setPincode(request.getPincode());
        doctor.setMedicalLicensePath(request.getMedicalLicensePath());
        doctor.setDegreeCertificatesPath(request.getDegreeCertificatesPath());

        // Save both user and doctor
        userRepository.save(user);
        doctorRepository.save(doctor);

        // Return response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Profile saved. Waiting for admin verification.");

        return response;
    }

    /**
     * Complete Admin Profile (Legacy method)
     */
    @Transactional
    public Map<String, Object> completeAdminProfile(CompleteAdminProfileRequest request, String proofUrl) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("User is not an admin");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Create or update admin profile
        Admin admin = adminRepository.findById(user.getId()).orElse(null);

        boolean isNewAdmin = (admin == null);
        if (isNewAdmin) {
            admin = new Admin();
            admin.setUser(user);
            admin.setId(user.getId());
        }

        admin.setPhone(request.getPhone());
        admin.setDesignation(request.getDesignation());
        admin.setDepartment(request.getDepartment());
        admin.setProofUrl(proofUrl);

        userRepository.save(user);
        adminRepository.save(admin);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Profile saved. Waiting for admin verification.");

        return response;
    }

    /**
     * Register Admin (Invitation-based)
     * Creates admin user with temporary password and sends invitation email
     */
    @Transactional
    public Map<String, Object> registerAdmin(AdminRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // Generate temporary password (will be changed during profile completion)
        String tempPassword = UUID.randomUUID().toString().substring(0, 12);

        // Create User with temporary password
        User user = new User();
        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(User.Role.ADMIN);
        user.setStatus(VerificationStatus.PENDING);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user = userRepository.save(user);

        // Create Admin Profile (minimal, will be completed later)
        Admin admin = new Admin();
        admin.setUser(user);
        admin.setId(user.getId());
        admin.setVerificationStatus(VerificationStatus.PENDING);
        admin.setDeleted(false);
        adminRepository.save(admin);

        // Send invitation email with temporary credentials
        emailService.sendAdminInvitationEmail(
                request.getEmail(),
                request.getFullName(),
                tempPassword);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Admin invitation sent successfully. Check email for login credentials.");
        response.put("userId", user.getId());
        return response;
    }

    /**
     * Login
     */
    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {

                    return new BadCredentialsException("Incorrect email or password.");
                });

        // Verify password
        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            throw new BadCredentialsException("Incorrect email or password.");
        }

        // Check if account is active
        if (user.getStatus() != VerificationStatus.ACTIVE) {

            throw new DisabledException("Your account is under verification. Please wait for approval.");
        }

        // Also check if admin is deleted (soft delete logic for admins)
        if (user.getRole() == User.Role.ADMIN) {
            // Admin admin = adminRepository.findByUserId(user.getId())... check deleted
            // But let's assume PENDING/ACTIVE status covers it for now as per previous
            // logic.
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getId(), user.getRole().name());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("role", user.getRole().name());
        response.put("userId", user.getId());

        return response;
    }
}
