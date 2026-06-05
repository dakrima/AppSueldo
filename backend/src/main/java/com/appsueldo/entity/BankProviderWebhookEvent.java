package com.appsueldo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "bank_provider_webhook_events")
public class BankProviderWebhookEvent extends BaseTimestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BankProvider provider;

    @Column(nullable = false, length = 120)
    private String providerEventId;

    @Column(nullable = false, length = 120)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BankProviderWebhookEventStatus status = BankProviderWebhookEventStatus.RECEIVED;

    @Column(nullable = false)
    private Instant receivedAt = Instant.now();

    private Instant processedAt;

    @Column(length = 120)
    private String errorCode;

    public Long getId() {
        return id;
    }

    public BankProvider getProvider() {
        return provider;
    }

    public void setProvider(BankProvider provider) {
        this.provider = provider;
    }

    public String getProviderEventId() {
        return providerEventId;
    }

    public void setProviderEventId(String providerEventId) {
        this.providerEventId = providerEventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public BankProviderWebhookEventStatus getStatus() {
        return status;
    }

    public void setStatus(BankProviderWebhookEventStatus status) {
        this.status = status;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
