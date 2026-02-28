package com.vinu.expensestore.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExpenseEvent {

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("merchant")
    private String merchant;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("user_id")
    private Integer userId;
}
