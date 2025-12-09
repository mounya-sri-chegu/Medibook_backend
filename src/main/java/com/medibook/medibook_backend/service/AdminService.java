package com.medibook.medibook_backend.service;

import com.medibook.medibook_backend.entity.*;
import com.medibook.medibook_backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminRepository adminRepository;
    private final EmailService emailService;

    public AdminService(UserRepository userRepository, PatientRepository patientRepository,
            DoctorRepository doctorRepository, AdminRepository adminRepository,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.adminRepository = adminRepository;
        this.emailService = emailService;
    }

    /**
     * Get all pending users (ADMIN, PATIENT, DOCTOR)
     */
    /**
     * Get all pending users (ADMIN, PATIENT, DOCTOR)
     */
    public List<Map<String, Object>> getPendingUsers() {
        List<User> pendingUsers = userRepository.findByStatus(VerificationStatus.PENDING);
        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : pendingUsers) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("name", user.getName());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole().name());
            userInfo.put("status", user.getStatus());
            userInfo.put("createdAt", user.getCreatedAt());

            // Add role-specific profile data
            // Add role-specific profile data
            // Add role-specific profile data
            if (user.getRole() == User.Role.PATIENT) {
                patientRepository.findByUserId(user.getId()).ifPresent(patient -> {
                    userInfo.put("dateOfBirth", patient.getDateOfBirth());
                    userInfo.put("gender", patient.getGender());
                    userInfo.put("bloodGroup", patient.getBloodGroup());
                    userInfo.put("phone", patient.getPhone());
                    userInfo.put("address", patient.getAddress());
                    userInfo.put("city", patient.getCity());
                    userInfo.put("state", patient.getState());
                    userInfo.put("country", patient.getCountry());
                    userInfo.put("pincode", patient.getPincode());
                    userInfo.put("idProofPath", patient.getIdProofPath());
                    userInfo.put("registrationDate", patient.getRegistrationDate());
                });
            } else if (user.getRole() == User.Role.DOCTOR) {
                doctorRepository.findByUserId(user.getId()).ifPresent(doctor -> {
                    userInfo.put("dateOfBirth", doctor.getDateOfBirth());
                    userInfo.put("gender", doctor.getGender());
                    userInfo.put("profilePhotoPath", doctor.getProfilePhotoPath());
                    userInfo.put("medicalRegistrationNumber", doctor.getMedicalRegistrationNumber());
                    userInfo.put("licensingAuthority", doctor.getLicensingAuthority());
                    userInfo.put("specialization", doctor.getSpecialization());
                    userInfo.put("qualification", doctor.getQualification());
                    userInfo.put("experience", doctor.getExperience());
                    userInfo.put("phone", doctor.getPhone());
                    userInfo.put("clinicHospitalName", doctor.getClinicHospitalName());
                    userInfo.put("city", doctor.getCity());
                    userInfo.put("state", doctor.getState());
                    userInfo.put("country", doctor.getCountry());
                    userInfo.put("pincode", doctor.getPincode());
                    userInfo.put("medicalLicensePath", doctor.getMedicalLicensePath());
                    userInfo.put("degreeCertificatesPath", doctor.getDegreeCertificatesPath());
                });
            } else if (user.getRole() == User.Role.ADMIN) {
                adminRepository.findByUserId(user.getId()).ifPresent(admin -> {
                    userInfo.put("phone", admin.getPhone());
                    userInfo.put("designation", admin.getDesignation());
                    userInfo.put("department", admin.getDepartment());
                });
            }

            result.add(userInfo);
        }

        return result;
    }

    /**
     * Approve a user
     */
    /**
     * Approve a user
     */
    @Transactional
    public Map<String, Object> approveUser(Long userId) {
        // log.info ... (need to add logger field to class first if not there. 349
        // didn't show logger field)
        // I'll add logger field at top of class via separate replacement or just ignore
        // log here if risky.
        // Actually I should add Logger.

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() == VerificationStatus.ACTIVE) {
            throw new RuntimeException("User already verified.");
        }

        // Set status to ACTIVE
        user.setStatus(VerificationStatus.ACTIVE);
        userRepository.save(user);

        // Send approval email
        emailService.sendApprovalEmailNew(user.getEmail(), user.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Verification approved successfully.");

        return response;
    }

    /**
     * Reject a user
     */
    @Transactional
    public Map<String, Object> rejectUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Option 1: Delete the user (and cascading will delete profile)
        userRepository.delete(user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false); // Per requirement for denial
        response.put("message", "Verification request denied by admin.");

        return response;
    }
}
