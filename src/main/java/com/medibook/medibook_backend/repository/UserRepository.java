package com.medibook.medibook_backend.repository;

import com.medibook.medibook_backend.entity.User;
import com.medibook.medibook_backend.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatus(VerificationStatus status);

    List<User> findByRoleAndStatus(User.Role role, VerificationStatus status);
}
