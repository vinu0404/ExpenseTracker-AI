package com.vinu.authservice.kafka;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreatedEvent {
    private String email;
    private String name;
    private String userName;
    private Long userId;
}
