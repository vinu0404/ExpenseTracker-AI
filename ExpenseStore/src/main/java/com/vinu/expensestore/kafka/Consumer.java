package com.vinu.expensestore.kafka;

import com.vinu.expensestore.entity.ExpenseInfo;
import com.vinu.expensestore.mapper.ExpenseMapper;
import com.vinu.expensestore.repository.ExpenseInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;


@Slf4j
@RequiredArgsConstructor
@Component
public class Consumer {
    private final ObjectMapper objectMapper;
    private final ExpenseInfoRepository expenseInfoRepository;

    @KafkaListener(topics = "ai_service", groupId = "expense-group")
    public void consume(byte[] message) {
        log.info("Received Kafka message on topic 'ai_service' ({} bytes)", message.length);
        try {
            ExpenseEvent event = objectMapper.readValue(message, ExpenseEvent.class);
            log.info("Deserialized AI expense event: userId={}, merchant={}, amount={}", event.getUserId(), event.getMerchant(), event.getAmount());
            processExpense(event);
        } catch (Exception e) {
            log.error("Failed to process AI expense event: {}", e.getMessage(), e);
        }
    }
    private void processExpense(ExpenseEvent event) {
        ExpenseInfo expenseInfo = ExpenseMapper.fromEvent(event);
        ExpenseInfo saved = expenseInfoRepository.save(expenseInfo);
        log.info("AI expense saved to DB: id={}, userId={}, merchant={}, amount={}", saved.getId(), event.getUserId(), event.getMerchant(), event.getAmount());
    }
}
