package com.vinu.expensestore.dto;

import com.vinu.expensestore.Constants.ExpenseCategory;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseRequestDto {


    @NotBlank(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Merchant is required")
    private String merchant;

    @NotBlank(message = "Currency is required")
    private String currency;

    private String description;

    private ExpenseCategory category;

    @NotBlank(message = "Expense date is required")
    private LocalDateTime expenseDate;


}
