package com.vinu.authservice.controllers;

import com.vinu.authservice.entity.RefreshToken;
import com.vinu.authservice.entity.UserInfo;
import com.vinu.authservice.repository.RefreshTokenRepository;
import com.vinu.authservice.repository.UserRepository;
import com.vinu.authservice.service.AdminService;
import com.vinu.authservice.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;


@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/promote/{username}")
    public ResponseEntity<ApiResponse<String>> promote(
            @PathVariable String username) {
        String result = adminService.promoteToAdmin(username);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("User promoted successfully")
                .data(result)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserInfo>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/admin/tokens")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RefreshToken>> getAllTokens() {
        return ResponseEntity.ok(refreshTokenRepository.findAll());
    }
}
