package com.vinu.userservice.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@Builder
@Getter
@Setter
public class User {

    @Id
    private Long userId;

    private String name;

    private String userName;

    private String email;


}
