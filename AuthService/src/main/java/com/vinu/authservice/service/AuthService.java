package com.vinu.authservice.service;


import com.vinu.authservice.dto.LoginRequest;
import com.vinu.authservice.dto.SignUpRequest;
import com.vinu.authservice.entity.RefreshToken;
import com.vinu.authservice.entity.UserInfo;
import com.vinu.authservice.entity.UserRole;
import com.vinu.authservice.kafka.UserCreatedEvent;
import com.vinu.authservice.kafka.UserEventProducer;
import com.vinu.authservice.repository.RefreshTokenRepository;
import com.vinu.authservice.repository.RoleRepository;
import com.vinu.authservice.repository.UserRepository;
import com.vinu.authservice.security.JwtService;
import com.vinu.authservice.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserEventProducer userEventProducer;

    public String doSignUp(SignUpRequest signUpRequest){
        log.info("Signup attempt for username: {}", signUpRequest.getUserName());
        if(userRepository.existsByUserName(signUpRequest.getUserName())){
            log.warn("Signup rejected - username already exists: {}", signUpRequest.getUserName());
            throw new RuntimeException("Username already exists");
        }
        UserRole roleUser = roleRepository.findByRoleName("ROLE_USER").orElseThrow(
                ()->new RuntimeException("Error from our side"));

        UserInfo userInfo = UserInfo.builder()
                .email(signUpRequest.getEmail())
                .name(signUpRequest.getName())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .roles(Set.of(roleUser))
                .userName(signUpRequest.getUserName())
                .build();
        userRepository.save(userInfo);
        log.info("User saved to DB: userId={}, username={}", userInfo.getUserId(), userInfo.getUserName());
        UserCreatedEvent event = new UserCreatedEvent(userInfo.getEmail(),
                userInfo.getName(),
                userInfo.getUserName(),
                userInfo.getUserId());
        userEventProducer.sendUserCreatedEvent(event);
        log.info("Signup completed for username: {}", signUpRequest.getUserName());
        return "User Registered Successfully";

    }

    public Map<String, String > doLogin(LoginRequest request){
        log.info("Login attempt for username: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                request.getUsername());
        log.info("Login successful for username: {}", request.getUsername());
        return Map.of("accessToken",accessToken,
                "refreshToken",refreshToken.getToken());
    }

    public Map<String,String> doRefreshToken(String requestRefreshToken){
        log.info("Refresh token request received");
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found");
                    return new RuntimeException("Refresh token not found");
                });
        refreshTokenService.verifyExpiration(refreshToken);
        UserInfo user = refreshToken.getUserInfo();
        refreshTokenRepository.delete(refreshToken);
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getUserName());

        String newAccessToken = jwtService.generateToken(userDetails);
        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(user.getUserName());
        log.info("Token refreshed for user: {}", user.getUserName());
        return Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken.getToken()
        );
    }



}