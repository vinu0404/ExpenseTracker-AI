package com.vinu.expensestore.controller;
import com.vinu.expensestore.dto.ExpenseRequestDto;
import com.vinu.expensestore.dto.ExpenseResponseDto;
import com.vinu.expensestore.service.ByUserExpense;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/saveExpense")
@RequiredArgsConstructor
public class ExpenseSaveController {

    private final ByUserExpense byUserExpense;

    @PostMapping
    public ResponseEntity<ExpenseResponseDto> saveByUser(
            @RequestHeader("user-id") Long userId,
            @Valid @RequestBody ExpenseRequestDto dto) {
        log.info("POST /saveExpense - userId: {}, merchant: {}, amount: {}", userId, dto.getMerchant(), dto.getAmount());
        return ResponseEntity.ok(byUserExpense.expenseSaveByUser(dto, userId));
    }

    @GetMapping
    public ResponseEntity<Page<ExpenseResponseDto>> getAllExpenses(
            @RequestHeader("user-id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("GET /saveExpense - userId: {}, page: {}, size: {}", userId, page, size);
        return ResponseEntity.ok(byUserExpense.getAllExpenses(userId, page, size, sortDir));
    }

}


