package com.appsueldo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.BankConnectionStatus;
import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.User;
import com.appsueldo.repository.BankAccountRepository;
import com.appsueldo.repository.BankConnectionRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private BankConnectionRepository bankConnectionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Test
    void createsManualConnectionAndAccountForUserWhenMissing() {
        User user = user();
        BankAccountService service = new BankAccountService(bankConnectionRepository, bankAccountRepository);
        when(bankAccountRepository.findFirstByUserAndBankConnectionProviderOrderByIdAsc(user, BankProvider.MANUAL))
            .thenReturn(Optional.empty());
        when(bankConnectionRepository.findFirstByUserAndProviderOrderByIdAsc(user, BankProvider.MANUAL))
            .thenReturn(Optional.empty());
        when(bankConnectionRepository.save(any(BankConnection.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount account = service.getOrCreateManualAccount(user);

        assertThat(account.getUser()).isSameAs(user);
        assertThat(account.getName()).isEqualTo("Cuenta manual");
        assertThat(account.getAccountType()).isEqualTo("MANUAL");
        assertThat(account.getCurrency()).isEqualTo("CLP");
        assertThat(account.getBankConnection().getUser()).isSameAs(user);
        assertThat(account.getBankConnection().getProvider()).isEqualTo(BankProvider.MANUAL);
        assertThat(account.getBankConnection().getStatus()).isEqualTo(BankConnectionStatus.ACTIVE);
        verify(bankConnectionRepository).save(any(BankConnection.class));
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    private User user() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@appsueldo.local");
        return user;
    }
}
