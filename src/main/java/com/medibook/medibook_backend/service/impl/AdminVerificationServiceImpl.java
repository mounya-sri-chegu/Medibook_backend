package com.medibook.medibook_backend.service.impl;

import com.medibook.medibook_backend.dto.AdminRoleVerificationDto;
import com.medibook.medibook_backend.entity.Admin;
import com.medibook.medibook_backend.entity.User;
import com.medibook.medibook_backend.entity.VerificationStatus;
import com.medibook.medibook_backend.exception.UnauthorizedActionException;
import com.medibook.medibook_backend.repository.AdminRepository;
import com.medibook.medibook_backend.repository.UserRepository;
import com.medibook.medibook_backend.service.AdminVerificationService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminVerificationServiceImpl implements AdminVerificationService {

    private static final Logger log = LoggerFactory.getLogger(AdminVerificationServiceImpl.class);

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final com.medibook.medibook_backend.service.EmailService emailService;

    public AdminVerificationServiceImpl(AdminRepository adminRepository,
            UserRepository userRepository,
            com.medibook.medibook_backend.service.EmailService emailService) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public Page<AdminRoleVerificationDto> getPendingAdmins(String search, Pageable pageable) {
        Page<Admin> admins;
        if (search != null && !search.isEmpty()) {
            admins = adminRepository.findByVerificationStatusAndDeletedFalseAndUser_NameContainingIgnoreCase(
                    VerificationStatus.PENDING, search, pageable);
        } else {
            admins = adminRepository.findByVerificationStatusAndDeletedFalse(
                    VerificationStatus.PENDING, pageable);
        }

        return admins.map(admin -> new AdminRoleVerificationDto(
                admin.getId(),
                admin.getUser().getName(),
                admin.getUser().getEmail(),
                admin.getUser().getRole().name(),
                admin.getVerificationStatus(),
                admin.getProofUrl(),
                admin.getUser().getCreatedAt()));
    }

    @Override
    @Transactional
    public void approveAdmin(Long targetAdminId, Long currentAdminId) {
        log.info("Admin {} attempting to approve Admin {}", currentAdminId, targetAdminId);

        // 1. Load current admin
        Admin currentAdmin = adminRepository.findById(currentAdminId)
                .orElseThrow(() -> new EntityNotFoundException("Current admin not found with ID: " + currentAdminId));

        if (currentAdmin.getVerificationStatus() != VerificationStatus.ACTIVE) {
            throw new AccessDeniedException("Only active admins can approve requests.");
        }

        // 2. Check if current admin is super admin OR the latest approved admin
        boolean isSuperAdmin = currentAdmin.getIsSuperAdmin() != null && currentAdmin.getIsSuperAdmin();

        if (!isSuperAdmin) {
            // Only check "latest admin" rule if not super admin
            Admin latestAdmin = adminRepository
                    .findFirstByVerificationStatusAndDeletedFalseOrderByApprovedAtDesc(VerificationStatus.ACTIVE)
                    .orElseThrow(() -> new EntityNotFoundException("No active admins found in system."));

            if (!latestAdmin.getId().equals(currentAdminId)) {
                log.warn("Approval denied: Admin {} is not the latest (Latest is {})", currentAdminId,
                        latestAdmin.getId());
                throw new UnauthorizedActionException("Only the latest verified admin can perform this action.");
            }
        }

        // 3. Load target admin
        Admin targetAdmin = adminRepository.findById(targetAdminId)
                .orElseThrow(() -> new EntityNotFoundException("Target admin not found with ID: " + targetAdminId));

        if (targetAdmin.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new IllegalArgumentException("Admin is not pending verification");
        }

        if (targetAdmin.isDeleted()) {
            throw new EntityNotFoundException("Target admin not found (deleted)");
        }

        // 4. Approve
        targetAdmin.setVerificationStatus(VerificationStatus.ACTIVE);
        targetAdmin.setApprovedAt(LocalDateTime.now());
        targetAdmin.setApprovedByAdminId(currentAdminId);

        // Sync User status
        User user = targetAdmin.getUser();
        if (user == null) {
            throw new EntityNotFoundException("User not found for admin ID: " + targetAdminId);
        }
        user.setStatus(VerificationStatus.ACTIVE);
        userRepository.save(user); // Save user first

        adminRepository.save(targetAdmin);

        // 5. Send approval email
        try {
            emailService.sendApprovalEmailNew(user.getEmail(), user.getName());
        } catch (Exception e) {
            log.error("Failed to send approval email to admin: {}", user.getEmail(), e);
            // Don't rollback transaction just because email failed
        }

        log.info("Admin {} approved successfully by {}", targetAdminId, currentAdminId);
    }

    @Override
    @Transactional
    public void denyAdmin(Long targetAdminId, Long currentAdminId) {
        log.info("Admin {} attempting to deny Admin {}", currentAdminId, targetAdminId);

        // 1. Load current admin & check latest
        Admin currentAdmin = adminRepository.findById(currentAdminId)
                .orElseThrow(() -> new EntityNotFoundException("Current admin not found"));

        if (currentAdmin.getVerificationStatus() != VerificationStatus.ACTIVE) {
            throw new AccessDeniedException("Only active admins can deny requests.");
        }

        Admin latestAdmin = adminRepository
                .findFirstByVerificationStatusAndDeletedFalseOrderByApprovedAtDesc(VerificationStatus.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("No active admins found."));

        if (!latestAdmin.getId().equals(currentAdminId)) {
            log.warn("Denial denied: Admin {} is not the latest (Latest is {})", currentAdminId, latestAdmin.getId());
            throw new UnauthorizedActionException("Only the latest verified admin can perform this action.");
        }

        // 2. Load target
        Admin targetAdmin = adminRepository.findById(targetAdminId)
                .orElseThrow(() -> new EntityNotFoundException("Target admin not found"));

        if (targetAdmin.isDeleted()) {
            throw new EntityNotFoundException("Target admin not found");
        }

        // 3. Deny (Soft Delete)
        targetAdmin.setDeleted(true);
        // We leave verificationStatus as PENDING

        adminRepository.save(targetAdmin);
        log.info("Admin {} denied (deleted) successfully by {}", targetAdminId, currentAdminId);
    }
}
