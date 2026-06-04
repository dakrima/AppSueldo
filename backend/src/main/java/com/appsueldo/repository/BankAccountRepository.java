package com.appsueldo.repository;

import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByUserOrderByNameAsc(User user);

    Optional<BankAccount> findByIdAndUser(Long id, User user);

    Optional<BankAccount> findFirstByUserAndBankConnectionProviderOrderByIdAsc(User user, BankProvider provider);

    Optional<BankAccount> findByUserAndExternalId(User user, String externalId);
}
