package com.vinu.authservice.security;

import com.vinu.authservice.entity.RefreshToken;
import com.vinu.authservice.entity.UserInfo;
import com.vinu.authservice.repository.RefreshTokenRepository;
import com.vinu.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private static final long REFRESH_TOKEN_DURATION = 7 * 24 * 60 * 60; // 7 days

    public RefreshToken createRefreshToken(String username) {
        UserInfo user = userRepository.findByUserName(username) .orElseThrow(
                () -> new RuntimeException("User not found"));
       // refreshTokenRepository.deleteByUserInfo_UserId(user.getUserId());
        // for deleting all token for that user single session  for baking app
        RefreshToken refreshToken = RefreshToken.builder()
                .userInfo(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(REFRESH_TOKEN_DURATION))
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }


    public void verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Login again.");
        }
    }


}
