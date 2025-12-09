package com.medibook.medibook_backend.controller;

import com.medibook.medibook_backend.dto.CompleteAdminProfileRequest;
import com.medibook.medibook_backend.service.AdminService;
import com.medibook.medibook_backend.service.AuthService;
import com.medibook.medibook_backend.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthService authService;
    private final AdminService adminService;
    private final FileStorageService fileStorageService;

    public AdminController(AuthService authService, AdminService adminService, FileStorageService fileStorageService) {
        this.authService = authService;
        this.adminService = adminService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * PUT /admin/profile
     * Complete admin profile with certificate upload
     */
    @PutMapping(value = "/profile", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> completeProfile(
            @RequestParam("userId") Long userId,
            @RequestParam("password") String password,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "designation", required = false) String designation,
            @RequestParam(value = "department", required = false) String department,
            @RequestPart("certificate") MultipartFile certificateFile) {
        try {
            CompleteAdminProfileRequest request = new CompleteAdminProfileRequest();
            request.setUserId(userId);
            request.setPassword(password);
            request.setPhone(phone);
            request.setDesignation(designation);
            request.setDepartment(department);

            // Handle file upload
            if (certificateFile == null || certificateFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Certificate is required for admin verification."));
            }

            // Validate format
            String contentType = certificateFile.getContentType();
            if (contentType == null || (!contentType.equals("application/pdf") && !contentType.startsWith("image/"))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message",
                                "Invalid certificate format. Please upload PDF/JPEG."));
            }

            String savedUrl;
            try {
                savedUrl = fileStorageService.saveAdminCertificate(certificateFile);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Unable to save certificate. Try again later."));
            }

            Map<String, Object> response = authService.completeAdminProfile(request, savedUrl);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * GET /admin/pending-users
     * Get all pending users (requires ADMIN role)
     */
    @GetMapping("/pending-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getPendingUsers() {
        try {
            List<Map<String, Object>> users = adminService.getPendingUsers();
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * POST /admin/users/{id}/approve
     * Approve a user (requires ADMIN role)
     */
    @PostMapping("/users/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveUser(@PathVariable Long id) {
        try {
            Map<String, Object> response = adminService.approveUser(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * POST /admin/users/{id}/reject
     * Reject a user (requires ADMIN role)
     */
    @PostMapping("/users/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectUser(@PathVariable Long id) {
        try {
            Map<String, Object> response = adminService.rejectUser(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
