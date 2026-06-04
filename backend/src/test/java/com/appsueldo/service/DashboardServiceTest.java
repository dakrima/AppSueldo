package com.appsueldo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.appsueldo.dto.MonthlySummaryDto;
import com.appsueldo.entity.TransactionType;
import com.appsueldo.entity.User;
import com.appsueldo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void monthlySummaryUsesCurrentUsersRealTransactionsAndIgnoresTransfersInBalance() {
        User user = user("test@appsueldo.local");
        DashboardService service = new DashboardService(transactionRepository, new FakeCurrentUserService(user));
        YearMonth currentMonth = YearMonth.now();
        LocalDate from = currentMonth.atDay(1);
        LocalDate to = currentMonth.atEndOfMonth();
        when(transactionRepository.sumByUserAndTypeBetweenDates(user, TransactionType.INCOME, from, to))
            .thenReturn(new BigDecimal("850000.00"));
        when(transactionRepository.sumByUserAndTypeBetweenDates(user, TransactionType.EXPENSE, from, to))
            .thenReturn(new BigDecimal("412300.00"));
        when(transactionRepository.countByUserAndTransactionDateBetween(user, from, to)).thenReturn(4L);

        MonthlySummaryDto summary = service.monthlySummary();

        assertThat(summary.monthlyIncome()).isEqualByComparingTo("850000.00");
        assertThat(summary.monthlyExpenses()).isEqualByComparingTo("412300.00");
        assertThat(summary.monthlyBalance()).isEqualByComparingTo("437700.00");
        assertThat(summary.estimatedSavings()).isEqualByComparingTo("437700.00");
        assertThat(summary.availableBalance()).isEqualByComparingTo("437700.00");
        assertThat(summary.transactionCount()).isEqualTo(4L);
    }

    private User user(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        return user;
    }

    private static class FakeCurrentUserService extends CurrentUserService {
        private final User user;

        FakeCurrentUserService(User user) {
            super(null, null);
            this.user = user;
        }

        @Override
        public User currentUser() {
            return user;
        }
    }
}
