package com.vinu.expensestore.repository;

import com.vinu.expensestore.entity.ExpenseInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseInfoRepository extends JpaRepository<ExpenseInfo,Long> {
    Page<ExpenseInfo> findByUserId(Integer userId, Pageable pageable);
}
