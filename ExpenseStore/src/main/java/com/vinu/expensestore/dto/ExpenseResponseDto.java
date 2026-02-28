package com.vinu.expensestore.dto;

import com.vinu.expensestore.Constants.ExpenseCategory;
import com.vinu.expensestore.Constants.ExpenseSource;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExpenseResponseDto {

    private Long id;
    private BigDecimal amount;
    private String merchant;
    private String currency;
    private String description;
    private ExpenseSource source;
    private ExpenseCategory category;
    private LocalDateTime expenseDate;
    private LocalDateTime createdAt;
}