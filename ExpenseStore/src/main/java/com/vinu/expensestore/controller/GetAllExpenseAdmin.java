package com.vinu.expensestore.controller;

import com.vinu.expensestore.dto.ExpenseResponseDto;
import com.vinu.expensestore.mapper.ExpenseMapper;
import com.vinu.expensestore.repository.ExpenseInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/expenses/admin")
@RequiredArgsConstructor
public class GetAllExpenseAdmin {

    private final ExpenseInfoRepository expenseInfoRepository;

    @GetMapping
    public ResponseEntity<Page<ExpenseResponseDto>> getExpensesByUser(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Admin fetching expenses for userId={}, page={}, size={}", userId, page, size);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by("expenseDate").ascending()
                : Sort.by("expenseDate").descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ExpenseResponseDto> expenses = expenseInfoRepository
                .findByUserId(userId, pageable)
                .map(ExpenseMapper::toResponse);

        return ResponseEntity.ok(expenses);
    }
}
