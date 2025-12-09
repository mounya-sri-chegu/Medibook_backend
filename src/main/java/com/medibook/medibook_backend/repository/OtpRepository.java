package com.medibook.medibook_backend.repository;

import com.medibook.medibook_backend.entity.Otp;
import com.medibook.medibook_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByUserIdAndRoleAndUsedFalse(Long userId, User.Role role);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndRole(Long userId, User.Role role);
}
