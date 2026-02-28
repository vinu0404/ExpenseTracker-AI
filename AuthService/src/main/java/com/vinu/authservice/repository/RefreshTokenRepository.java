package com.vinu.authservice.repository;

import com.vinu.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

   Optional<RefreshToken> findByToken(String token);
   void deleteByUserInfo_UserId(Long userId);
}
