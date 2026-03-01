package com.vinu.authservice.controllers;

import com.vinu.authservice.dto.LoginRequest;
import com.vinu.authservice.dto.RefreshTokenRequest;
import com.vinu.authservice.dto.SignUpRequest;
import com.vinu.authservice.service.AuthService;
import com.vinu.authservice.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {


    private final AuthService authService;


    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signup(
            @RequestBody SignUpRequest request) {

        try {
            String result = authService.doSignUp(request);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message("User registered successfully")
                    .data(result)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(409).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(
            @RequestBody LoginRequest request) {

        Map<String, String > tokens = authService.doLogin(request);

        ApiResponse<Map<String, String>> response =
                ApiResponse.<Map<String, String>>builder()
                        .success(true)
                        .message("Login successful")
                        .data(tokens)
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }


    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String,String>>> refresh(
            @RequestBody RefreshTokenRequest request) {

        Map<String, String> tokens =
                authService.doRefreshToken(request.getRefreshToken());

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String,String>>builder()
                .success(true)
                .message("Access token & Refresh Token refreshed successfully")
                .data(tokens)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }


}
