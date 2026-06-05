package com.appsueldo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.appsueldo.config.FintocProperties;
import com.appsueldo.dto.BankConnectionResponse;
import com.appsueldo.dto.BankConnectionSyncResponse;
import com.appsueldo.dto.CreateFintocLinkIntentResponse;
import com.appsueldo.dto.ExchangeFintocTokenRequest;
import com.appsueldo.dto.fintoc.FintocInstitutionResponse;
import com.appsueldo.dto.fintoc.FintocLinkIntentRequest;
import com.appsueldo.dto.fintoc.FintocLinkIntentResponse;
import com.appsueldo.dto.fintoc.FintocLinkResponse;
import com.appsueldo.dto.fintoc.FintocRefreshIntentMfaResponse;
import com.appsueldo.dto.fintoc.FintocRefreshIntentResponse;
import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.BankConnectionStatus;
import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.User;
import com.appsueldo.exception.BadRequestException;
import com.appsueldo.exception.ResourceNotFoundException;
import com.appsueldo.repository.BankConnectionRepository;
import com.appsueldo.service.bankprovider.BankProviderClient;
import com.appsueldo.service.fintoc.FintocClientException;
import com.appsueldo.service.fintoc.FintocTokenCrypto;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FintocConnectionServiceTest {

    @Mock
    private BankProviderClient bankProviderClient;

    @Mock
    private BankConnectionRepository bankConnectionRepository;

    private FintocTokenCrypto fintocTokenCrypto;
    private FakeFintocSyncService fintocSyncService;
    private FakeCurrentUserService currentUserService;
    private FintocConnectionService service;

    @BeforeEach
    void setUp() {
        FintocProperties properties = properties();
        fintocTokenCrypto = new FintocTokenCrypto(properties);
        fintocSyncService = new FakeFintocSyncService();
        currentUserService = new FakeCurrentUserService();
        service = new FintocConnectionService(
            bankProviderClient,
            fintocTokenCrypto,
            properties,
            currentUserService,
            bankConnectionRepository,
            fintocSyncService
        );
    }

    @Test
    void createLinkIntentUsesMovementsChileIndividualAndReturnsSafeResponse() {
        User user = user();
        currentUserService.setUser(user);
        when(bankProviderClient.createLinkIntent(any(FintocLinkIntentRequest.class))).thenReturn(
            new FintocLinkIntentResponse(
                "li_123",
                "link_intent",
                "created",
                "movements",
                "cl",
                "individual",
                "widget_token_secret",
                null
            )
        );

        CreateFintocLinkIntentResponse response = service.createLinkIntentForCurrentUser();

        ArgumentCaptor<FintocLinkIntentRequest> captor = ArgumentCaptor.forClass(FintocLinkIntentRequest.class);
        verify(bankProviderClient).createLinkIntent(captor.capture());
        assertThat(captor.getValue().product()).isEqualTo("movements");
        assertThat(captor.getValue().country()).isEqualTo("cl");
        assertThat(captor.getValue().holderType()).isEqualTo("individual");
        assertThat(response.provider()).isEqualTo(BankProvider.FINTOC);
        assertThat(response.publicKey()).isEqualTo("pk_test_public");
        assertThat(response.widgetToken()).isEqualTo("widget_token_secret");
        assertThat(CreateFintocLinkIntentResponse.class.getRecordComponents())
            .extracting(component -> component.getName())
            .doesNotContain("secretKey", "linkToken", "accessTokenRef", "exchangeToken");
        assertThat(currentUserService.calls()).isEqualTo(1);
    }

    @Test
    void exchangeRejectsBlankExchangeToken() {
        assertThatThrownBy(() -> service.exchangeForCurrentUser(new ExchangeFintocTokenRequest(" ")))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("exchangeToken es requerido.");

        verifyNoInteractions(bankProviderClient, bankConnectionRepository);
        assertThat(currentUserService.calls()).isZero();
    }

    @Test
    void exchangeCreatesEncryptedConnectionAndRunsInitialSync() {
        User user = user();
        FintocLinkResponse link = linkResponse("active");
        BankAccount account = account(user, "Cuenta Vista");
        fintocSyncService.result = FintocSyncResult.completed(2, 1, List.of(account));
        currentUserService.setUser(user);
        when(bankProviderClient.exchangeToken("exchange_token_secret")).thenReturn(link);
        when(bankConnectionRepository.findByUserAndProviderAndProviderConnectionId(
            user,
            BankProvider.FINTOC,
            "link_123"
        )).thenReturn(Optional.empty());
        when(bankConnectionRepository.save(any(BankConnection.class))).thenAnswer(invocation -> {
            BankConnection connection = invocation.getArgument(0);
            setId(connection, 10L);
            return connection;
        });

        BankConnectionResponse response = service.exchangeForCurrentUser(
            new ExchangeFintocTokenRequest("exchange_token_secret")
        );

        ArgumentCaptor<BankConnection> connectionCaptor = ArgumentCaptor.forClass(BankConnection.class);
        verify(bankConnectionRepository).save(connectionCaptor.capture());
        BankConnection savedConnection = connectionCaptor.getValue();
        assertThat(savedConnection.getUser()).isSameAs(user);
        assertThat(savedConnection.getProvider()).isEqualTo(BankProvider.FINTOC);
        assertThat(savedConnection.getProviderConnectionId()).isEqualTo("link_123");
        assertThat(savedConnection.getInstitutionName()).isEqualTo("Banco Estado");
        assertThat(savedConnection.getStatus()).isEqualTo(BankConnectionStatus.ACTIVE);
        assertThat(savedConnection.getAccessTokenRef()).startsWith("v1:");
        assertThat(savedConnection.getAccessTokenRef()).doesNotContain("link_token_secret");
        assertThat(savedConnection.getAccessTokenRef()).isNotEqualTo("link_token_secret");

        assertThat(fintocSyncService.calls).isEqualTo(1);
        assertThat(fintocSyncService.user).isSameAs(user);
        assertThat(fintocSyncService.connection).isSameAs(savedConnection);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.provider()).isEqualTo(BankProvider.FINTOC);
        assertThat(response.accounts()).hasSize(1);
        assertThat(response.importedTransactionsCount()).isEqualTo(2);
        assertThat(response.skippedTransactionsCount()).isEqualTo(1);
        assertThat(response.syncStatus()).isEqualTo("COMPLETED");
        assertThat(response.toString()).doesNotContain(
            "link_token_secret",
            "exchange_token_secret",
            savedConnection.getAccessTokenRef()
        );
    }

    @Test
    void repeatedExchangeUpdatesExistingConnectionAndAccountWithoutDuplicating() {
        User user = user();
        BankConnection existingConnection = new BankConnection();
        existingConnection.setUser(user);
        existingConnection.setProvider(BankProvider.FINTOC);
        existingConnection.setProviderConnectionId("link_123");
        existingConnection.setAccessTokenRef(fintocTokenCrypto.encrypt("old_link_token"));
        setId(existingConnection, 10L);

        BankAccount existingAccount = new BankAccount();
        existingAccount.setUser(user);
        existingAccount.setBankConnection(existingConnection);
        existingAccount.setExternalId("acc_123");
        existingAccount.setName("Cuenta antigua");
        setId(existingAccount, 20L);
        fintocSyncService.result = FintocSyncResult.completed(0, 1, List.of(existingAccount));

        currentUserService.setUser(user);
        when(bankProviderClient.exchangeToken("exchange_token_secret")).thenReturn(linkResponse("active"));
        when(bankConnectionRepository.findByUserAndProviderAndProviderConnectionId(
            user,
            BankProvider.FINTOC,
            "link_123"
        )).thenReturn(Optional.of(existingConnection));
        when(bankConnectionRepository.save(existingConnection)).thenReturn(existingConnection);

        BankConnectionResponse response = service.exchangeForCurrentUser(
            new ExchangeFintocTokenRequest("exchange_token_secret")
        );

        verify(bankConnectionRepository).save(existingConnection);
        assertThat(existingConnection.getAccessTokenRef()).doesNotContain("link_token_secret");
        assertThat(fintocSyncService.calls).isEqualTo(1);
        assertThat(fintocSyncService.connection).isSameAs(existingConnection);
        assertThat(response.accounts()).hasSize(1);
        assertThat(response.importedTransactionsCount()).isZero();
        assertThat(response.skippedTransactionsCount()).isEqualTo(1);
        assertThat(response.syncStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void exchangeKeepsConnectionWhenInitialSyncFails() {
        User user = user();
        currentUserService.setUser(user);
        fintocSyncService.fail = true;
        when(bankProviderClient.exchangeToken("exchange_token_secret")).thenReturn(linkResponse("active"));
        when(bankConnectionRepository.findByUserAndProviderAndProviderConnectionId(
            user,
            BankProvider.FINTOC,
            "link_123"
        )).thenReturn(Optional.empty());
        when(bankConnectionRepository.save(any(BankConnection.class))).thenAnswer(invocation -> {
            BankConnection connection = invocation.getArgument(0);
            setId(connection, 10L);
            return connection;
        });

        BankConnectionResponse response = service.exchangeForCurrentUser(
            new ExchangeFintocTokenRequest("exchange_token_secret")
        );

        ArgumentCaptor<BankConnection> connectionCaptor = ArgumentCaptor.forClass(BankConnection.class);
        verify(bankConnectionRepository).save(connectionCaptor.capture());
        assertThat(connectionCaptor.getValue().getAccessTokenRef()).doesNotContain("link_token_secret");
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.accounts()).isEmpty();
        assertThat(response.importedTransactionsCount()).isZero();
        assertThat(response.skippedTransactionsCount()).isZero();
        assertThat(response.syncStatus()).isEqualTo("ERROR");
    }

    @Test
    void syncConnectionRejectsMissingOrForeignConnection() {
        User user = user();
        currentUserService.setUser(user);
        when(bankConnectionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.syncConnectionForCurrentUser(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Conexion bancaria no encontrada.");

        verifyNoInteractions(bankProviderClient);
        assertThat(fintocSyncService.calls).isZero();
    }

    @Test
    void syncConnectionRejectsNonFintocConnection() {
        User user = user();
        currentUserService.setUser(user);
        BankConnection connection = new BankConnection();
        connection.setUser(user);
        connection.setProvider(BankProvider.MANUAL);
        setId(connection, 20L);
        when(bankConnectionRepository.findByIdAndUser(20L, user)).thenReturn(Optional.of(connection));

        assertThatThrownBy(() -> service.syncConnectionForCurrentUser(20L))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Solo se pueden sincronizar conexiones Fintoc.");

        verifyNoInteractions(bankProviderClient);
        assertThat(fintocSyncService.calls).isZero();
    }

    @Test
    void syncConnectionCreatesRefreshIntentAndRunsSafeSyncWhenMfaIsNotRequired() {
        User user = user();
        BankConnection connection = fintocConnection(user);
        BankAccount account = account(user, "Cuenta Vista");
        currentUserService.setUser(user);
        fintocSyncService.result = FintocSyncResult.completed(4, 2, List.of(account));
        when(bankConnectionRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(connection));
        when(bankProviderClient.createRefreshIntent("link_token_secret", "only_last"))
            .thenReturn(refreshIntent("created", null));

        BankConnectionSyncResponse response = service.syncConnectionForCurrentUser(10L);

        verify(bankProviderClient).createRefreshIntent("link_token_secret", "only_last");
        assertThat(fintocSyncService.calls).isEqualTo(1);
        assertThat(fintocSyncService.user).isSameAs(user);
        assertThat(fintocSyncService.connection).isSameAs(connection);
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.requiresMfa()).isFalse();
        assertThat(response.widgetToken()).isNull();
        assertThat(response.importedTransactionsCount()).isEqualTo(4);
        assertThat(response.skippedTransactionsCount()).isEqualTo(2);
        assertThat(response.syncStatus()).isEqualTo("COMPLETED");
        assertThat(response.toString()).doesNotContain("link_token_secret", "sk_test_secret");
    }

    @Test
    void syncConnectionReturnsMfaWidgetTokenWithoutRunningSync() {
        User user = user();
        BankConnection connection = fintocConnection(user);
        currentUserService.setUser(user);
        when(bankConnectionRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(connection));
        when(bankProviderClient.createRefreshIntent("link_token_secret", "only_last"))
            .thenReturn(refreshIntent("created", "ri_widget_token_secret"));

        BankConnectionSyncResponse response = service.syncConnectionForCurrentUser(10L);

        assertThat(response.status()).isEqualTo("REQUIRES_MFA");
        assertThat(response.requiresMfa()).isTrue();
        assertThat(response.widgetToken()).isEqualTo("ri_widget_token_secret");
        assertThat(response.syncStatus()).isEqualTo("PENDING");
        assertThat(response.toString()).doesNotContain("ri_widget_token_secret", "link_token_secret");
        assertThat(fintocSyncService.calls).isZero();
    }

    @Test
    void syncConnectionRejectedRefreshIntentReturnsControlledError() {
        User user = user();
        BankConnection connection = fintocConnection(user);
        currentUserService.setUser(user);
        when(bankConnectionRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(connection));
        when(bankProviderClient.createRefreshIntent("link_token_secret", "only_last"))
            .thenReturn(refreshIntent("rejected", null));

        assertThatThrownBy(() -> service.syncConnectionForCurrentUser(10L))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("El banco rechazo la actualizacion de la conexion.")
            .message()
            .doesNotContain("link_token_secret");

        assertThat(fintocSyncService.calls).isZero();
    }

    @Test
    void syncConnectionFintocErrorsDoNotExposeTokens() {
        User user = user();
        BankConnection connection = fintocConnection(user);
        currentUserService.setUser(user);
        when(bankConnectionRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(connection));
        when(bankProviderClient.createRefreshIntent("link_token_secret", "only_last"))
            .thenThrow(new FintocClientException("Fintoc request failed while creating refresh intent."));

        assertThatThrownBy(() -> service.syncConnectionForCurrentUser(10L))
            .isInstanceOf(FintocClientException.class)
            .hasMessage("Fintoc request failed while creating refresh intent.")
            .message()
            .doesNotContain("link_token_secret");

        assertThat(fintocSyncService.calls).isZero();
    }

    private FintocProperties properties() {
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

    private User user() {
        User user = new User();
        user.setEmail("test@appsueldo.local");
        user.setName("Test User");
        return user;
    }

    private FintocLinkResponse linkResponse(String status) {
        return new FintocLinkResponse(
            "link_123",
            "link",
            status,
            "link_token_secret",
            new FintocInstitutionResponse("cl_banco_estado", "Banco Estado", "cl"),
            List.of()
        );
    }

    private BankAccount account(User user, String name) {
        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setName(name);
        account.setAccountType("sight_account");
        account.setCurrency("CLP");
        setId(account, 20L);
        return account;
    }

    private BankConnection fintocConnection(User user) {
        BankConnection connection = new BankConnection();
        connection.setUser(user);
        connection.setProvider(BankProvider.FINTOC);
        connection.setProviderConnectionId("link_123");
        connection.setAccessTokenRef(fintocTokenCrypto.encrypt("link_token_secret"));
        setId(connection, 10L);
        return connection;
    }

    private FintocRefreshIntentResponse refreshIntent(String status, String widgetToken) {
        FintocRefreshIntentMfaResponse mfa = widgetToken == null
            ? null
            : new FintocRefreshIntentMfaResponse(widgetToken);
        return new FintocRefreshIntentResponse(
            "ri_123",
            "refresh_intent",
            "link",
            "link_123",
            status,
            null,
            OffsetDateTime.parse("2026-06-04T12:00:00Z"),
            "only_last",
            null,
            mfa
        );
    }

    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not set entity id for test.", exception);
        }
    }

    private static class FakeCurrentUserService extends CurrentUserService {
        private User user;
        private int calls;

        FakeCurrentUserService() {
            super(null, null);
        }

        void setUser(User user) {
            this.user = user;
        }

        int calls() {
            return calls;
        }

        @Override
        public User currentUser() {
            calls++;
            return user;
        }
    }

    private static class FakeFintocSyncService extends FintocSyncService {
        private FintocSyncResult result = FintocSyncResult.completed(0, 0, List.of());
        private boolean fail;
        private User user;
        private BankConnection connection;
        private int calls;

        FakeFintocSyncService() {
            super(null, null, null, null, null, null);
        }

        @Override
        public FintocSyncResult syncInitial(User user, BankConnection connection) {
            calls++;
            this.user = user;
            this.connection = connection;
            if (fail) {
                throw new IllegalStateException("Fintoc request failed while listing movements.");
            }
            return result;
        }
    }
}
