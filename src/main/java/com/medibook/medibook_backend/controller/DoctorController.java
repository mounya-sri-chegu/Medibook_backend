package com.medibook.medibook_backend.controller;

import com.medibook.medibook_backend.dto.CompleteDoctorProfileRequest;
import com.medibook.medibook_backend.service.AuthService;
import com.medibook.medibook_backend.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/profile/doctor")
public class DoctorController {

    private final AuthService authService;
    private final FileStorageService fileStorageService;

    public DoctorController(AuthService authService, FileStorageService fileStorageService) {
        this.authService = authService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * PUT /profile/doctor
     * Complete doctor profile after OTP verification with certificate uploads
     */
    @PutMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> completeProfile(
            @RequestParam("userId") Long userId,
            @RequestParam("password") String password,
            @RequestParam("dateOfBirth") String dateOfBirth,
            @RequestParam("gender") String gender,
            @RequestParam("medicalRegistrationNumber") String medicalRegistrationNumber,
            @RequestParam("licensingAuthority") String licensingAuthority,
            @RequestParam("specialization") String specialization,
            @RequestParam("qualification") String qualification,
            @RequestParam("experience") Integer experience,
            @RequestParam("phone") String phone,
            @RequestParam("clinicHospitalName") String clinicHospitalName,
            @RequestParam("city") String city,
            @RequestParam("state") String state,
            @RequestParam("country") String country,
            @RequestParam("pincode") String pincode,
            @RequestPart(value = "profilePhoto", required = false) MultipartFile profilePhoto,
            @RequestPart("medicalLicense") MultipartFile medicalLicense,
            @RequestPart("degreeCertificates") MultipartFile degreeCertificates) {
        try {
            // Validate required files
            if (medicalLicense == null || medicalLicense.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Medical license is required."));
            }

            if (degreeCertificates == null || degreeCertificates.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Degree certificates are required."));
            }

            // Validate file formats
            String licenseContentType = medicalLicense.getContentType();
            String degreeContentType = degreeCertificates.getContentType();

            if (licenseContentType == null
                    || (!licenseContentType.equals("application/pdf") && !licenseContentType.startsWith("image/"))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message",
                                "Invalid medical license format. Please upload PDF/JPEG."));
            }

            if (degreeContentType == null
                    || (!degreeContentType.equals("application/pdf") && !degreeContentType.startsWith("image/"))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message",
                                "Invalid degree certificate format. Please upload PDF/JPEG."));
            }

            // Save files
            String medicalLicensePath;
            String degreeCertificatesPath;
            String profilePhotoPath = null;

            try {
                medicalLicensePath = fileStorageService.saveDoctorCertificate(medicalLicense);
                degreeCertificatesPath = fileStorageService.saveDoctorCertificate(degreeCertificates);

                if (profilePhoto != null && !profilePhoto.isEmpty()) {
                    profilePhotoPath = fileStorageService.saveDoctorCertificate(profilePhoto);
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Unable to save files. Try again later."));
            }

            // Build request object
            CompleteDoctorProfileRequest request = new CompleteDoctorProfileRequest();
            request.setUserId(userId);
            request.setPassword(password);
            request.setDateOfBirth(LocalDate.parse(dateOfBirth));
            request.setGender(gender);
            request.setProfilePhotoPath(profilePhotoPath);
            request.setMedicalRegistrationNumber(medicalRegistrationNumber);
            request.setLicensingAuthority(licensingAuthority);
            request.setSpecialization(specialization);
            request.setQualification(qualification);
            request.setExperience(experience);
            request.setPhone(phone);
            request.setClinicHospitalName(clinicHospitalName);
            request.setCity(city);
            request.setState(state);
            request.setCountry(country);
            request.setPincode(pincode);
            request.setMedicalLicensePath(medicalLicensePath);
            request.setDegreeCertificatesPath(degreeCertificatesPath);

            Map<String, Object> response = authService.completeDoctorProfile(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
