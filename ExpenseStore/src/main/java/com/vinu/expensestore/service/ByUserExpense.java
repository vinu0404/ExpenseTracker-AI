package com.vinu.expensestore.service;

import com.vinu.expensestore.dto.ExpenseRequestDto;
import com.vinu.expensestore.dto.ExpenseResponseDto;
import com.vinu.expensestore.entity.ExpenseInfo;
import com.vinu.expensestore.mapper.ExpenseMapper;
import com.vinu.expensestore.repository.ExpenseInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ByUserExpense {

    private final ExpenseInfoRepository expenseInfoRepository;

    public ExpenseResponseDto expenseSaveByUser(ExpenseRequestDto request, Long userId) {
        ExpenseInfo entity = ExpenseMapper.toEntity(request, userId);
        ExpenseInfo saved = expenseInfoRepository.save(entity);
        log.info("Manual expense saved: id={}, userId={}, amount={}", saved.getId(), userId, saved.getAmount());
        return ExpenseMapper.toResponse(saved);
    }

    public Page<ExpenseResponseDto> getAllExpenses(Long userId, int page, int size, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by("expenseDate").ascending()
                : Sort.by("expenseDate").descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return expenseInfoRepository.findByUserId(userId.intValue(), pageable)
                .map(ExpenseMapper::toResponse);
    }
}
