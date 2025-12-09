package com.medibook.medibook_backend.service;

import com.medibook.medibook_backend.dto.AdminRoleVerificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminVerificationService {

    Page<AdminRoleVerificationDto> getPendingAdmins(String search, Pageable pageable);

    void approveAdmin(Long targetAdminId, Long currentAdminId);

    void denyAdmin(Long targetAdminId, Long currentAdminId);
}
