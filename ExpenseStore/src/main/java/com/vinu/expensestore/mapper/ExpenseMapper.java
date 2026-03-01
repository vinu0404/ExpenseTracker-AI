package com.vinu.expensestore.mapper;
import com.vinu.expensestore.Constants.ExpenseSource;
import com.vinu.expensestore.dto.ExpenseRequestDto;
import com.vinu.expensestore.dto.ExpenseResponseDto;
import com.vinu.expensestore.entity.ExpenseInfo;
import com.vinu.expensestore.kafka.ExpenseEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class ExpenseMapper {

    public static ExpenseInfo toEntity(ExpenseRequestDto request, Long userId) {
        return ExpenseInfo.builder()
                .userId(userId.intValue())
                .amount(request.getAmount())
                .merchant(request.getMerchant())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .category(request.getCategory())
                .expenseDate(request.getExpenseDate())
                .source(ExpenseSource.YOU)
                .build();
    }


    public static ExpenseInfo fromEvent(ExpenseEvent event) {
        return ExpenseInfo.builder()
                .userId(event.getUserId())
                .amount(BigDecimal.valueOf(event.getAmount()))
                .merchant(event.getMerchant())
                .currency(event.getCurrency())
                .description(event.getDescription())
                .expenseDate(event.getCreatedAt() != null ? event.getCreatedAt() : LocalDateTime.now())
                .source(ExpenseSource.AI)
                .build();
    }

    public static ExpenseResponseDto toResponse(ExpenseInfo expense) {
        return ExpenseResponseDto.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .merchant(expense.getMerchant())
                .currency(expense.getCurrency())
                .description(expense.getDescription())
                .source(expense.getSource())
                .category(expense.getCategory())
                .expenseDate(expense.getExpenseDate())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}