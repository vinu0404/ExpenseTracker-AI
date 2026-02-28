package com.vinu.authservice.config;

import com.vinu.authservice.entity.UserInfo;
import com.vinu.authservice.entity.UserRole;
import com.vinu.authservice.repository.RoleRepository;
import com.vinu.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRole("ROLE_USER");
        seedRole("ROLE_ADMIN");
        seedAdminUser();
    }

    private void seedRole(String roleName) {
        if (roleRepository.findByRoleName(roleName).isEmpty()) {
            UserRole role = new UserRole();
            role.setRoleName(roleName);
            roleRepository.save(role);
            log.info("Seeded role: {}", roleName);
        } else {
            log.info("Role already exists, skipping: {}", roleName);
        }
    }

    private void seedAdminUser() {
        if (userRepository.existsByUserName("vinu")) {
            log.info("Admin user already exists, skipping.");
            return;
        }

        try {
            UserRole adminRole = roleRepository.findByRoleName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
            UserRole userRole = roleRepository.findByRoleName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

            UserInfo admin = UserInfo.builder()
                    .name("Vinay Kumar")
                    .userName("vinu")
                    .email("ursvinu18@gmail.com")
                    .password(passwordEncoder.encode("12345678"))
                    .roles(Set.of(adminRole, userRole))
                    .build();

            userRepository.save(admin);
            log.info("Seeded admin user: vinu");

        } catch (Exception e) {
            log.warn("Admin user seeding skipped (already exists or conflict): {}", e.getMessage());
        }
    }
}