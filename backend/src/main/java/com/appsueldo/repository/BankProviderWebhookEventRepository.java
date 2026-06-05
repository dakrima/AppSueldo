package com.appsueldo.repository;

import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.BankProviderWebhookEvent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankProviderWebhookEventRepository extends JpaRepository<BankProviderWebhookEvent, Long> {
    Optional<BankProviderWebhookEvent> findByProviderAndProviderEventId(
        BankProvider provider,
        String providerEventId
    );
}
