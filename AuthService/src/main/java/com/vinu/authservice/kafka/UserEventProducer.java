package com.vinu.authservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "user-created-topic";

    public void sendUserCreatedEvent(UserCreatedEvent event) {
        log.info("Publishing user-created event to Kafka: username={}, userId={}", event.getUserName(), event.getUserId());
        try {
            byte[] payload = objectMapper.writeValueAsBytes(event);
            kafkaTemplate.send(TOPIC, event.getUserName(), payload);
            log.info("Kafka event published successfully for username: {}", event.getUserName());
        } catch (Exception e) {
            log.error("Failed to publish Kafka event for username: {}", event.getUserName(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}