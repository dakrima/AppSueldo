package com.appsueldo.repository;

import com.appsueldo.entity.Transaction;
import com.appsueldo.entity.TransactionClassification;
import com.appsueldo.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionClassificationRepository extends JpaRepository<TransactionClassification, Long> {
    List<TransactionClassification> findByUser(User user);

    Optional<TransactionClassification> findByTransactionAndUser(Transaction transaction, User user);

    Optional<TransactionClassification> findByTransactionIdAndUser(Long transactionId, User user);
}
