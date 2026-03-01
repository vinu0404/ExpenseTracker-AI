package com.vinu.userservice.service;
import com.vinu.userservice.dto.UserDto;
import com.vinu.userservice.entity.User;
import com.vinu.userservice.kafka.UserCreatedEvent;
import com.vinu.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "user-created-topic", groupId = "user-service-group")
    public void consume(byte[] message) {
        log.info("Received Kafka message on topic 'user-created-topic'");
        try {
            UserCreatedEvent event =
                    objectMapper.readValue(message, UserCreatedEvent.class);
            log.info("Deserialized user-created event: userId={}, username={}", event.getUserId(), event.getUserName());
            User user = User.builder()
                    .userId(event.getUserId())
                    .userName(event.getUserName())
                    .email(event.getEmail())
                    .name(event.getName())
                    .build();
            userRepository.save(user);
            log.info("User saved to DB from Kafka event: userId={}, username={}", event.getUserId(), event.getUserName());
        } catch (Exception e) {
            log.error("Failed to process user-created event", e);
        }
    }


    public UserDto getUserInfo(Long userId){
        log.info("Fetching user info for userId: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(
                () -> {
                    log.warn("User not found for userId: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                }
        );
        return UserDto.builder()
                .userName(user.getUserName())
                .email(user.getEmail())
                .name(user.getName())
                .build();

    }


}
