package com.medibook.medibook_backend.controller;

import com.medibook.medibook_backend.dto.CompletePatientProfileRequest;
import com.medibook.medibook_backend.service.AuthService;
import com.medibook.medibook_backend.service.FileStorageService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/profile/patient")
public class PatientController {

    private final AuthService authService;
    private final FileStorageService fileStorageService;

    public PatientController(AuthService authService, FileStorageService fileStorageService) {
        this.authService = authService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * PUT /profile/patient
     * Complete patient profile after OTP verification with file upload
     */
    @PutMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> completeProfile(
            @RequestParam("userId") Long userId,
            @RequestParam("password") String password,
            @RequestParam("dateOfBirth") String dateOfBirthStr,
            @RequestParam("gender") String gender,
            @RequestParam("bloodGroup") String bloodGroup,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("city") String city,
            @RequestParam("state") String state,
            @RequestParam("country") String country,
            @RequestParam("pincode") String pincode,
            @RequestParam("idProof") MultipartFile idProof) {
        try {
            // Validate file
            if (idProof == null || idProof.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "ID Proof is required."));
            }

            String contentType = idProof.getContentType();
            if (contentType == null || (!contentType.equals("application/pdf") && !contentType.startsWith("image/"))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Invalid ID Proof format. Please upload PDF/JPEG."));
            }

            // Save ID Proof
            String idProofPath = fileStorageService.savePatientIdProof(idProof);

            // Create Request Object
            CompletePatientProfileRequest request = new CompletePatientProfileRequest();
            request.setUserId(userId);
            request.setPassword(password);
            request.setDateOfBirth(LocalDate.parse(dateOfBirthStr));
            request.setGender(gender);
            request.setBloodGroup(bloodGroup);
            request.setPhone(phone);
            request.setAddress(address);
            request.setCity(city);
            request.setState(state);
            request.setCountry(country);
            request.setPincode(pincode);
            request.setIdProofPath(idProofPath);

            Map<String, Object> response = authService.completePatientProfile(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
