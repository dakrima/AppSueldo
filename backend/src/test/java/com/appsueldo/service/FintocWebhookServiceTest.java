package com.appsueldo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appsueldo.config.FintocProperties;
import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.BankConnectionStatus;
import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.BankProviderWebhookEvent;
import com.appsueldo.entity.BankProviderWebhookEventStatus;
import com.appsueldo.entity.User;
import com.appsueldo.exception.BadRequestException;
import com.appsueldo.repository.BankAccountRepository;
import com.appsueldo.repository.BankConnectionRepository;
import com.appsueldo.repository.BankProviderWebhookEventRepository;
import com.appsueldo.service.fintoc.FintocWebhookSignatureVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FintocWebhookServiceTest {

    @Mock
    private BankProviderWebhookEventRepository webhookEventRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankConnectionRepository bankConnectionRepository;

    private FakeSignatureVerifier signatureVerifier;
    private FakeFintocSyncService fintocSyncService;
    private FintocWebhookService service;

    @BeforeEach
    void setUp() {
        signatureVerifier = new FakeSignatureVerifier();
        fintocSyncService = new FakeFintocSyncService();
        service = new FintocWebhookService(
            signatureVerifier,
            new ObjectMapper().findAndRegisterModules(),
            webhookEventRepository,
            bankAccountRepository,
            bankConnectionRepository,
            fintocSyncService
        );
    }

    @Test
    void succeededWebhookProcessesEventAndRunsSync() {
        BankAccount account = account();
        when(webhookEventRepository.findByProviderAndProviderEventId(BankProvider.FINTOC, "evt_123"))
            .thenReturn(Optional.empty());
        when(webhookEventRepository.save(any(BankProviderWebhookEvent.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(bankAccountRepository.findFirstByExternalIdAndBankConnectionProvider("acc_123", BankProvider.FINTOC))
            .thenReturn(Optional.of(account));

        service.handle(payload("evt_123", "account.refresh_intent.succeeded", "acc_123", "succeeded", 3), "valid");

        assertThat(signatureVerifier.calls).isEqualTo(1);
        assertThat(fintocSyncService.calls).isEqualTo(1);
        assertThat(fintocSyncService.user).isSameAs(account.getUser());
        assertThat(fintocSyncService.connection).isSameAs(account.getBankConnection());
        assertThat(savedEvent().getStatus()).isEqualTo(BankProviderWebhookEventStatus.PROCESSED);
        assertThat(savedEvent().getProcessedAt()).isNotNull();
    }

    @Test
    void duplicatedWebhookDoesNotRunSyncTwice() {
        BankProviderWebhookEvent existingEvent = event("evt_123", "account.refresh_intent.succeeded");
        existingEvent.setStatus(BankProviderWebhookEventStatus.PROCESSED);
        when(webhookEventRepository.findByProviderAndProviderEventId(BankProvider.FINTOC, "evt_123"))
            .thenReturn(Optional.of(existingEvent));

        service.handle(payload("evt_123", "account.refresh_intent.succeeded", "acc_123", "succeeded", 3), "valid");

        assertThat(fintocSyncService.calls).isZero();
        verify(bankAccountRepository, never()).findFirstByExternalIdAndBankConnectionProvider(any(), any());
    }

    @Test
    void failedWebhookIsStoredWithoutRunningSync() {
        BankAccount account = account();
        when(webhookEventRepository.findByProviderAndProviderEventId(BankProvider.FINTOC, "evt_failed"))
            .thenReturn(Optional.empty());
        when(webhookEventRepository.save(any(BankProviderWebhookEvent.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(bankAccountRepository.findFirstByExternalIdAndBankConnectionProvider("acc_123", BankProvider.FINTOC))
            .thenReturn(Optional.of(account));

        service.handle(
            payload("evt_failed", "account.refresh_intent.failed", "acc_123", "failed", 0),
            "valid"
        );

        assertThat(fintocSyncService.calls).isZero();
        assertThat(savedEvent().getStatus()).isEqualTo(BankProviderWebhookEventStatus.FAILED);
        assertThat(savedEvent().getErrorCode()).isEqualTo("retryable_error");
    }

    @Test
    void rejectedWebhookMarksConnectionAndDoesNotRunSync() {
        BankAccount account = account();
        when(webhookEventRepository.findByProviderAndProviderEventId(BankProvider.FINTOC, "evt_rejected"))
            .thenReturn(Optional.empty());
        when(webhookEventRepository.save(any(BankProviderWebhookEvent.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(bankAccountRepository.findFirstByExternalIdAndBankConnectionProvider("acc_123", BankProvider.FINTOC))
            .thenReturn(Optional.of(account));

        service.handle(
            payload("evt_rejected", "account.refresh_intent.rejected", "acc_123", "rejected", 0),
            "valid"
        );

        assertThat(fintocSyncService.calls).isZero();
        assertThat(account.getBankConnection().getStatus()).isEqualTo(BankConnectionStatus.ERROR);
        assertThat(savedEvent().getStatus()).isEqualTo(BankProviderWebhookEventStatus.REJECTED);
        verify(bankConnectionRepository).save(account.getBankConnection());
    }

    @Test
    void unknownWebhookEventIsIgnored() {
        when(webhookEventRepository.findByProviderAndProviderEventId(BankProvider.FINTOC, "evt_unknown"))
            .thenReturn(Optional.empty());
        when(webhookEventRepository.save(any(BankProviderWebhookEvent.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        service.handle(payload("evt_unknown", "link.credentials_changed", "link_123", "active", 0), "valid");

        assertThat(fintocSyncService.calls).isZero();
        assertThat(savedEvent().getStatus()).isEqualTo(BankProviderWebhookEventStatus.IGNORED);
        verify(bankAccountRepository, never()).findFirstByExternalIdAndBankConnectionProvider(any(), any());
    }

    @Test
    void invalidSignatureRejectsWebhookBeforePersistence() {
        signatureVerifier.fail = true;

        assertThatThrownBy(() -> service.handle(
            payload("evt_123", "account.refresh_intent.succeeded", "acc_123", "succeeded", 1),
            "invalid"
        ))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Firma Fintoc invalida.");

        verify(webhookEventRepository, never()).save(any());
        assertThat(fintocSyncService.calls).isZero();
    }

    private BankProviderWebhookEvent savedEvent() {
        return org.mockito.Mockito.mockingDetails(webhookEventRepository)
            .getInvocations()
            .stream()
            .filter(invocation -> invocation.getMethod().getName().equals("save"))
            .reduce((first, second) -> second)
            .map(invocation -> invocation.getArgument(0, BankProviderWebhookEvent.class))
            .orElseThrow();
    }

    private BankProviderWebhookEvent event(String providerEventId, String eventType) {
        BankProviderWebhookEvent event = new BankProviderWebhookEvent();
        event.setProvider(BankProvider.FINTOC);
        event.setProviderEventId(providerEventId);
        event.setEventType(eventType);
        return event;
    }

    private BankAccount account() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@appsueldo.local");

        BankConnection connection = new BankConnection();
        connection.setUser(user);
        connection.setProvider(BankProvider.FINTOC);
        connection.setStatus(BankConnectionStatus.ACTIVE);
        connection.setProviderConnectionId("link_123");

        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setBankConnection(connection);
        account.setExternalId("acc_123");
        account.setName("Cuenta Vista");
        account.setCurrency("CLP");
        return account;
    }

    private String payload(
        String id,
        String type,
        String refreshedObjectId,
        String status,
        int newMovements
    ) {
        String publicError = "failed".equals(status) ? "\"retryable_error\"" : "null";
        return """
            {
              "id": "%s",
              "type": "%s",
              "mode": "test",
              "created_at": "2026-06-04T12:00:00.000Z",
              "data": {
                "object": "refresh_intent",
                "refreshed_object": "account",
                "refreshed_object_id": "%s",
                "status": "%s",
                "public_error": %s,
                "created_at": "2026-06-04T12:00:00.000Z",
                "type": "only_last",
                "new_movements": %d
              },
              "object": "event"
            }
            """.formatted(id, type, refreshedObjectId, status, publicError, newMovements);
    }

    private static class FakeSignatureVerifier extends FintocWebhookSignatureVerifier {
        private boolean fail;
        private int calls;

        FakeSignatureVerifier() {
            super(properties());
        }

        @Override
        public void verify(String rawBody, String signatureHeader) {
            calls++;
            if (fail) {
                throw new BadRequestException("Firma Fintoc invalida.");
            }
        }

        private static FintocProperties properties() {
            return new FintocProperties(
                "sk_test_secret",
                "pk_test_public",
                "https://api.fintoc.com",
                "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                "test",
                90,
                "whsec_secret"
            );
        }
    }

    private static class FakeFintocSyncService extends FintocSyncService {
        private User user;
        private BankConnection connection;
        private int calls;

        FakeFintocSyncService() {
            super(null, null, null, null, null, null);
        }

        @Override
        public FintocSyncResult syncInitial(User user, BankConnection connection) {
            this.user = user;
            this.connection = connection;
            calls++;
            return FintocSyncResult.completed(1, 0, List.of());
        }
    }
}
