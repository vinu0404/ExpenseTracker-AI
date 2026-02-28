package com.vinu.authservice.repository;

import com.vinu.authservice.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserInfo,Long>{
    Optional<UserInfo>findByUserName(String userName);
    Boolean existsByUserName(String userName);

}
