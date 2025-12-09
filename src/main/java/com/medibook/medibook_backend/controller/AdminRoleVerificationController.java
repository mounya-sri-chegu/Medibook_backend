package com.medibook.medibook_backend.controller;

import com.medibook.medibook_backend.dto.AdminRoleVerificationDto;
import com.medibook.medibook_backend.entity.User;
import com.medibook.medibook_backend.payload.ApiResponse;
import com.medibook.medibook_backend.repository.UserRepository;
import com.medibook.medibook_backend.service.AdminVerificationService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.medibook.medibook_backend.exception.UnauthorizedActionException;

@RestController
@RequestMapping("/api/admin/role-verification")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleVerificationController {

    private static final Logger log = LoggerFactory.getLogger(AdminRoleVerificationController.class);

    private final AdminVerificationService verificationService;
    private final UserRepository userRepository;

    public AdminRoleVerificationController(AdminVerificationService verificationService,
            UserRepository userRepository) {
        this.verificationService = verificationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<AdminRoleVerificationDto>> getPendingAdmins(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<AdminRoleVerificationDto> result = verificationService.getPendingAdmins(search, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{adminId}/approve")
    public ResponseEntity<ApiResponse> approveAdmin(@PathVariable Long adminId) {
        try {
            Long currentAdminId = getCurrentUserId();
            log.info("Request to approve admin {} by {}", adminId, currentAdminId);
            verificationService.approveAdmin(adminId, currentAdminId);
            return ResponseEntity.ok(new ApiResponse(true, "Verification approved successfully."));
        } catch (EntityNotFoundException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (AccessDeniedException e) {
            log.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(403).body(new ApiResponse(false, e.getMessage()));
        } catch (UnauthorizedActionException e) {
            log.error("Unauthorized action: {}", e.getMessage());
            return ResponseEntity.status(403).body(new ApiResponse(false, e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error approving admin: ", e);
            return ResponseEntity.status(500).body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/{adminId}/deny")
    public ResponseEntity<ApiResponse> denyAdmin(@PathVariable Long adminId) {
        Long currentAdminId = getCurrentUserId();
        log.info("Request to deny admin {} by {}", adminId, currentAdminId);
        verificationService.denyAdmin(adminId, currentAdminId);
        // Requirement says: "Denial: { "success": false, "message": "Verification
        // request denied by admin." }"
        // But ApiResponse takes (success, message). So false means "failure" usually.
        // But here the ACTION of denying was SUCCESSFUL.
        // Wait, "Denial: { success: false, ... }" implies checking the response for
        // pending user?
        // OR does it mean the response TO THE ADMIN performing the denial?
        // If Admin denies a user, the Admin's action "Succeeded".
        // The text says "Ensure each approval/denial returns: Success: ... Denial: {
        // success: false ... }"
        // This is ambiguous. Does "Denial" mean "When a user CHECKS status"? No, use
        // context "Update Verification APIs (Patient... Admin)".
        // It likely means: When I call `POST /approve`, return `true`. When I call
        // `POST /deny`, return `true` with message "denied"?
        // OR does it mean "The result of the verification is Denial"?
        // BUT the user example shows:
        // Success: { success: true, message: "Verification approved successfully." }
        // Denial: { success: false, message: "Verification request denied by admin." }
        // If I return `success: false` to the Admin Dashboard, the dashboard might
        // think the API call FAILED (e.g. network error).
        // Usually, successful Denial operation returns 200 OK with `success: true`.
        // However, if the user explicitly wants `success: false` in the JSON body, I
        // will do it.
        // It might be interpreted as "The verification outcome is NEGATIVE".
        // Use `false`.
        return ResponseEntity.ok(new ApiResponse(false, "Verification request denied by admin."));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getId();
    }
}
