package com.vinu.authservice.service;


import com.vinu.authservice.entity.UserInfo;
import com.vinu.authservice.entity.UserRole;
import com.vinu.authservice.repository.RoleRepository;
import com.vinu.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    @Transactional
    public String promoteToAdmin(String userName){
        UserInfo user = userRepository.findByUserName(userName) .orElseThrow(
                () -> new RuntimeException("User not found"));

        UserRole adminRole = roleRepository.findByRoleName("ROLE_ADMIN") .orElseThrow(
                () -> new RuntimeException("Admin role missing"));
        user.getRoles().add(adminRole);
        userRepository.save(user);
        return  userName + " promoted to ADMIN";
    }
}
