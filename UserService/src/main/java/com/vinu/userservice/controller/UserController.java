package com.vinu.userservice.controller;
import com.vinu.userservice.dto.UserDto;
import com.vinu.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Map<String,Object> getInfo(@RequestHeader("user-id") Long userId) {
        log.info("GET /users - userId: {}", userId);
        UserDto userInfo = userService.getUserInfo(userId);
         return Map.of("username",userInfo.getUserName(),"name",userInfo.getName(),
                "email",userInfo.getEmail());
    }

}
