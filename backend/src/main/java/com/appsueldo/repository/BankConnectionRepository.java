package com.appsueldo.repository;

import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankConnectionRepository extends JpaRepository<BankConnection, Long> {
    List<BankConnection> findByUserOrderByCreatedAtDesc(User user);

    Optional<BankConnection> findByIdAndUser(Long id, User user);

    Optional<BankConnection> findFirstByUserAndProviderOrderByIdAsc(User user, BankProvider provider);

    Optional<BankConnection> findByUserAndProviderAndProviderConnectionId(
        User user,
        BankProvider provider,
        String providerConnectionId
    );
}
