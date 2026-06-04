package com.appsueldo.repository;

import com.appsueldo.entity.Transaction;
import com.appsueldo.entity.TransactionSource;
import com.appsueldo.entity.TransactionType;
import com.appsueldo.entity.User;
import com.appsueldo.entity.BankAccount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByTransactionDateDescIdDesc(User user);

    List<Transaction> findByBankAccountOrderByTransactionDateDescIdDesc(BankAccount bankAccount);

    Optional<Transaction> findByIdAndUser(Long id, User user);

    boolean existsByBankAccountAndSourceAndExternalId(BankAccount bankAccount, TransactionSource source, String externalId);

    @Query("""
        select coalesce(sum(t.amount), 0)
        from Transaction t
        where t.user = :user
          and t.type = :type
          and t.transactionDate between :from and :to
        """)
    BigDecimal sumByUserAndTypeBetweenDates(
        @Param("user") User user,
        @Param("type") TransactionType type,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    long countByUserAndTransactionDateBetween(User user, LocalDate from, LocalDate to);
}
