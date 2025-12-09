package com.medibook.medibook_backend.config;

import com.medibook.medibook_backend.entity.Admin;
import com.medibook.medibook_backend.entity.User;
import com.medibook.medibook_backend.entity.VerificationStatus;
import com.medibook.medibook_backend.repository.AdminRepository;
import com.medibook.medibook_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Or inject PasswordEncoder
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (adminRepository.count() == 0) {
            log.info("Seeding default System Admin...");

            String email = "chmounyasri@gmail.com";
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                // Create User
                user = new User();
                user.setName("System Admin");
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode("Admin123"));
                user.setRole(User.Role.ADMIN);
                user.setStatus(VerificationStatus.ACTIVE);
                user = userRepository.save(user);
                log.info("Created new user for System Admin.");
            } else {
                log.info("Using existing user for System Admin: {}", email);
                // Ensure role is ADMIN
                if (user.getRole() != User.Role.ADMIN) {
                    user.setRole(User.Role.ADMIN);
                    user = userRepository.save(user);
                }
            }

            // Create Admin Profile
            Admin admin = new Admin(user);
            admin.setVerificationStatus(VerificationStatus.ACTIVE);
            admin.setApprovedAt(LocalDateTime.now());
            admin.setIsSuperAdmin(true);
            admin.setDeleted(false);
            // approvedByAdminId is null for the first admin

            // Due to @MapsId, admin.id matches user.id.
            // Admin implements Persistable<Long> to allow save() to treat it as NEW
            // (insert) even with ID set.
            adminRepository.save(admin);

            log.info("Default System Admin seeded successfully.");
        }
    }
}
