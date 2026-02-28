package com.vinu.authservice.controllers;
import com.vinu.authservice.entity.UserInfo;
import com.vinu.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ping")
@RequiredArgsConstructor
public class UserIdController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Long> getUserId() {
    Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
        UserInfo userInfo = userRepository
                .findByUserName(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(userInfo.getUserId());
    }
    return ResponseEntity.status(401).build();
}
}
