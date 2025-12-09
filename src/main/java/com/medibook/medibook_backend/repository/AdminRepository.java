package com.medibook.medibook_backend.repository;

import com.medibook.medibook_backend.entity.Admin;
import com.medibook.medibook_backend.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUserId(Long userId);

    Optional<Admin> findFirstByVerificationStatusAndDeletedFalseOrderByApprovedAtDesc(VerificationStatus status);

    // Search by User's name
    Page<Admin> findByVerificationStatusAndDeletedFalseAndUser_NameContainingIgnoreCase(
            VerificationStatus status, String name, Pageable pageable);

    // List all (when search is empty)
    Page<Admin> findByVerificationStatusAndDeletedFalse(
            VerificationStatus status, Pageable pageable);
}
