package com.appsueldo.service;

import com.appsueldo.dto.MonthlySummaryDto;
import com.appsueldo.entity.TransactionType;
import com.appsueldo.entity.User;
import com.appsueldo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    public DashboardService(TransactionRepository transactionRepository, CurrentUserService currentUserService) {
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public MonthlySummaryDto monthlySummary() {
        User user = currentUserService.currentUser();
        YearMonth currentMonth = YearMonth.now();
        LocalDate from = currentMonth.atDay(1);
        LocalDate to = currentMonth.atEndOfMonth();

        BigDecimal income = transactionRepository.sumByUserAndTypeBetweenDates(user, TransactionType.INCOME, from, to);
        BigDecimal expenses = transactionRepository.sumByUserAndTypeBetweenDates(user, TransactionType.EXPENSE, from, to);
        BigDecimal estimatedSavings = income.subtract(expenses);

        return new MonthlySummaryDto(income, expenses, estimatedSavings, estimatedSavings);
    }
}
