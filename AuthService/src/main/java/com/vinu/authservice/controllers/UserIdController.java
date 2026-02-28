package com.vinu.authservice.controllers;
import com.vinu.authservice.entity.UserInfo;
import com.vinu.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ping")
@RequiredArgsConstructor
public class UserIdController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Void> getUserId() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            UserInfo userInfo = userRepository
                    .findByUserName(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            log.info("Auth validated for user: {}, userId: {}", authentication.getName(), userInfo.getUserId());
            return ResponseEntity.ok()
                    .header("X-User-Id", String.valueOf(userInfo.getUserId()))
                    .header("X-User-Name", userInfo.getUserName())
                    .build();
        }
        log.warn("Unauthorized ping request");
        return ResponseEntity.status(401).build();
    }
}
