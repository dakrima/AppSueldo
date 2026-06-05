package com.appsueldo.service;

import com.appsueldo.dto.fintoc.FintocWebhookEventData;
import com.appsueldo.dto.fintoc.FintocWebhookEventPayload;
import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.BankConnectionStatus;
import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.BankProviderWebhookEvent;
import com.appsueldo.entity.BankProviderWebhookEventStatus;
import com.appsueldo.exception.BadRequestException;
import com.appsueldo.repository.BankAccountRepository;
import com.appsueldo.repository.BankConnectionRepository;
import com.appsueldo.repository.BankProviderWebhookEventRepository;
import com.appsueldo.service.fintoc.FintocWebhookSignatureVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FintocWebhookService {

    private static final String EVENT_REFRESH_SUCCEEDED = "account.refresh_intent.succeeded";
    private static final String EVENT_REFRESH_FAILED = "account.refresh_intent.failed";
    private static final String EVENT_REFRESH_REJECTED = "account.refresh_intent.rejected";
    private static final Set<String> SUPPORTED_EVENTS = Set.of(
        EVENT_REFRESH_SUCCEEDED,
        EVENT_REFRESH_FAILED,
        EVENT_REFRESH_REJECTED
    );

    private final FintocWebhookSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;
    private final BankProviderWebhookEventRepository webhookEventRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankConnectionRepository bankConnectionRepository;
    private final FintocSyncService fintocSyncService;

    public FintocWebhookService(
        FintocWebhookSignatureVerifier signatureVerifier,
        ObjectMapper objectMapper,
        BankProviderWebhookEventRepository webhookEventRepository,
        BankAccountRepository bankAccountRepository,
        BankConnectionRepository bankConnectionRepository,
        FintocSyncService fintocSyncService
    ) {
        this.signatureVerifier = signatureVerifier;
        this.objectMapper = objectMapper;
        this.webhookEventRepository = webhookEventRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.bankConnectionRepository = bankConnectionRepository;
        this.fintocSyncService = fintocSyncService;
    }

    @Transactional
    public void handle(String rawBody, String signatureHeader) {
        signatureVerifier.verify(rawBody, signatureHeader);

        FintocWebhookEventPayload payload = parse(rawBody);
        RegisteredWebhookEvent registeredEvent = registerEvent(payload);
        if (!registeredEvent.isNew()) {
            return;
        }

        processEvent(registeredEvent.event(), payload);
    }

    private FintocWebhookEventPayload parse(String rawBody) {
        try {
            return objectMapper.readValue(rawBody, FintocWebhookEventPayload.class);
        } catch (JsonProcessingException exception) {
            throw new BadRequestException("Webhook Fintoc invalido.");
        }
    }

    private RegisteredWebhookEvent registerEvent(FintocWebhookEventPayload payload) {
        String providerEventId = requireNotBlank(payload.id(), "Webhook Fintoc invalido.");
        String eventType = requireNotBlank(payload.type(), "Webhook Fintoc invalido.");

        return webhookEventRepository.findByProviderAndProviderEventId(BankProvider.FINTOC, providerEventId)
            .map(event -> new RegisteredWebhookEvent(event, false))
            .orElseGet(() -> {
                BankProviderWebhookEvent event = new BankProviderWebhookEvent();
                event.setProvider(BankProvider.FINTOC);
                event.setProviderEventId(providerEventId);
                event.setEventType(eventType);
                event.setStatus(BankProviderWebhookEventStatus.RECEIVED);
                event.setReceivedAt(Instant.now());
                return new RegisteredWebhookEvent(webhookEventRepository.save(event), true);
            });
    }

    private void processEvent(BankProviderWebhookEvent event, FintocWebhookEventPayload payload) {
        if (!SUPPORTED_EVENTS.contains(payload.type())) {
            mark(event, BankProviderWebhookEventStatus.IGNORED, "UNSUPPORTED_EVENT");
            return;
        }

        FintocWebhookEventData data = payload.data();
        String accountExternalId = data == null ? null : data.refreshedObjectId();
        if (accountExternalId == null || accountExternalId.isBlank()) {
            mark(event, BankProviderWebhookEventStatus.ERROR, "MISSING_ACCOUNT");
            return;
        }
        BankAccount bankAccount = bankAccountRepository
            .findFirstByExternalIdAndBankConnectionProvider(accountExternalId.trim(), BankProvider.FINTOC)
            .orElse(null);
        if (bankAccount == null) {
            mark(event, BankProviderWebhookEventStatus.ERROR, "UNKNOWN_ACCOUNT");
            return;
        }

        if (EVENT_REFRESH_SUCCEEDED.equals(payload.type())) {
            syncAccountConnection(event, bankAccount);
            return;
        }

        if (EVENT_REFRESH_REJECTED.equals(payload.type())) {
            markConnectionNeedsReview(bankAccount.getBankConnection());
            mark(event, BankProviderWebhookEventStatus.REJECTED, safeErrorCode(data));
            return;
        }

        mark(event, BankProviderWebhookEventStatus.FAILED, safeErrorCode(data));
    }

    private void syncAccountConnection(BankProviderWebhookEvent event, BankAccount bankAccount) {
        try {
            // TODO: Optimize webhook sync to fetch only refreshed_object_id when Fintoc account-level sync is added.
            fintocSyncService.syncInitial(bankAccount.getUser(), bankAccount.getBankConnection());
            mark(event, BankProviderWebhookEventStatus.PROCESSED, null);
        } catch (RuntimeException exception) {
            mark(event, BankProviderWebhookEventStatus.ERROR, "SYNC_ERROR");
        }
    }

    private void markConnectionNeedsReview(BankConnection connection) {
        connection.setStatus(BankConnectionStatus.ERROR);
        bankConnectionRepository.save(connection);
    }

    private void mark(
        BankProviderWebhookEvent event,
        BankProviderWebhookEventStatus status,
        String errorCode
    ) {
        event.setStatus(status);
        event.setProcessedAt(Instant.now());
        event.setErrorCode(errorCode);
        webhookEventRepository.save(event);
    }

    private String safeErrorCode(FintocWebhookEventData data) {
        if (data == null || data.publicError() == null || data.publicError().isBlank()) {
            return null;
        }
        String value = data.publicError().trim();
        return value.length() <= 120 ? value : value.substring(0, 120);
    }

    private String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private record RegisteredWebhookEvent(
        BankProviderWebhookEvent event,
        boolean isNew
    ) {
    }
}
