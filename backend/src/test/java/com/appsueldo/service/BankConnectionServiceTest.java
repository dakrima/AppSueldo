package com.appsueldo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appsueldo.dto.BankConnectionResponse;
import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.BankConnectionStatus;
import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.User;
import com.appsueldo.repository.BankAccountRepository;
import com.appsueldo.repository.BankConnectionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BankConnectionServiceTest {

    @Mock
    private BankConnectionRepository bankConnectionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Test
    void listCurrentUserConnectionsIncludesAccountsAndOmitsSensitiveFields() {
        User user = user("test@appsueldo.local");
        BankConnection connection = manualConnection(user);
        setId(connection, 10L);
        connection.setAccessTokenRef("vault://secret/provider-token");
        BankAccount account = manualAccount(user, connection);
        setId(account, 20L);
        BankConnectionService service = new BankConnectionService(
            bankConnectionRepository,
            bankAccountRepository,
            new FakeBankAccountService(account),
            new FakeCurrentUserService(user)
        );
        when(bankAccountRepository.findByUserOrderByNameAsc(user)).thenReturn(List.of(account));
        when(bankConnectionRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(connection));

        List<BankConnectionResponse> response = service.listCurrentUserConnections();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(10L);
        assertThat(response.get(0).provider()).isEqualTo(BankProvider.MANUAL);
        assertThat(response.get(0).accounts()).hasSize(1);
        assertThat(response.get(0).accounts().get(0).id()).isEqualTo(20L);
        assertThat(BankConnectionResponse.class.getRecordComponents())
            .extracting(component -> component.getName())
            .doesNotContain("accessTokenRef", "providerConnectionId");
        verify(bankConnectionRepository).findByUserOrderByCreatedAtDesc(user);
        verify(bankAccountRepository).findByUserOrderByNameAsc(user);
    }

    private User user(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        return user;
    }

    private BankConnection manualConnection(User user) {
        BankConnection connection = new BankConnection();
        connection.setUser(user);
        connection.setProvider(BankProvider.MANUAL);
        connection.setInstitutionName("Manual");
        connection.setStatus(BankConnectionStatus.ACTIVE);
        return connection;
    }

    private BankAccount manualAccount(User user, BankConnection connection) {
        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setBankConnection(connection);
        account.setName("Cuenta manual");
        account.setAccountType("MANUAL");
        account.setCurrency("CLP");
        return account;
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

    private static class FakeBankAccountService extends BankAccountService {
        private final BankAccount bankAccount;

        FakeBankAccountService(BankAccount bankAccount) {
            super(null, null);
            this.bankAccount = bankAccount;
        }

        @Override
        public BankAccount getOrCreateManualAccount(User user) {
            return bankAccount;
        }
    }
}
