package com.appsueldo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appsueldo.config.FintocProperties;
import com.appsueldo.dto.fintoc.FintocAccountResponse;
import com.appsueldo.dto.fintoc.FintocBalanceResponse;
import com.appsueldo.dto.fintoc.FintocMovementResponse;
import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.Transaction;
import com.appsueldo.entity.TransactionSource;
import com.appsueldo.entity.TransactionType;
import com.appsueldo.entity.User;
import com.appsueldo.repository.BankAccountRepository;
import com.appsueldo.repository.TransactionRepository;
import com.appsueldo.service.bankprovider.BankProviderClient;
import com.appsueldo.service.fintoc.FintocClientException;
import com.appsueldo.service.fintoc.FintocMovementMapper;
import com.appsueldo.service.fintoc.FintocTokenCrypto;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FintocSyncServiceTest {

    @Mock
    private BankProviderClient bankProviderClient;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private FintocSyncService service;
    private FintocTokenCrypto tokenCrypto;

    @BeforeEach
    void setUp() {
        FintocProperties properties = properties();
        tokenCrypto = new FintocTokenCrypto(properties);
        service = new FintocSyncService(
            bankProviderClient,
            tokenCrypto,
            new FintocMovementMapper(),
            properties,
            bankAccountRepository,
            transactionRepository
        );
    }

    @Test
    void syncStoresNewMovementsAsInternalTransactions() {
        User user = user();
        BankConnection connection = connection(user);
        BankAccount account = account(user, connection);
        when(bankProviderClient.listAccounts("link_token_secret")).thenReturn(List.of(accountResponse("acc_123")));
        when(bankAccountRepository.findByUserAndBankConnectionAndExternalId(user, connection, "acc_123"))
            .thenReturn(Optional.of(account));
        when(bankAccountRepository.save(account)).thenReturn(account);
        when(bankProviderClient.listMovements(
            eq("link_token_secret"),
            eq("acc_123"),
            any(LocalDate.class),
            isNull(),
            eq(1),
            eq(300),
            eq(true)
        )).thenReturn(List.of(
            movement("mov_income", 10000L, "Sueldo", "CLP"),
            movement("mov_expense", -12990L, "Supermercado", "usd")
        ));
        when(transactionRepository.existsByBankAccountAndSourceAndExternalId(account, TransactionSource.FINTOC, "mov_income"))
            .thenReturn(false);
        when(transactionRepository.existsByBankAccountAndSourceAndExternalId(account, TransactionSource.FINTOC, "mov_expense"))
            .thenReturn(false);

        FintocSyncResult result = service.syncInitial(user, connection);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(captor.capture());
        List<Transaction> transactions = captor.getAllValues();
        assertThat(transactions).hasSize(2);
        assertThat(transactions.get(0).getUser()).isSameAs(user);
        assertThat(transactions.get(0).getBankAccount()).isSameAs(account);
        assertThat(transactions.get(0).getSource()).isEqualTo(TransactionSource.FINTOC);
        assertThat(transactions.get(0).getExternalId()).isEqualTo("mov_income");
        assertThat(transactions.get(0).getType()).isEqualTo(TransactionType.INCOME);
        assertThat(transactions.get(0).getAmount()).isEqualByComparingTo("10000");
        assertThat(transactions.get(0).getCurrency()).isEqualTo("CLP");
        assertThat(transactions.get(0).getDescription()).isEqualTo("Sueldo");
        assertThat(transactions.get(1).getExternalId()).isEqualTo("mov_expense");
        assertThat(transactions.get(1).getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transactions.get(1).getAmount()).isEqualByComparingTo("12990");
        assertThat(transactions.get(1).getCurrency()).isEqualTo("USD");
        assertThat(result.importedTransactionsCount()).isEqualTo(2);
        assertThat(result.skippedTransactionsCount()).isZero();
        assertThat(result.syncStatus()).isEqualTo("COMPLETED");
        assertThat(result.accounts()).containsExactly(account);
    }

    @Test
    void syncSkipsExistingMovementWithoutDuplicatingIt() {
        User user = user();
        BankConnection connection = connection(user);
        BankAccount account = account(user, connection);
        when(bankProviderClient.listAccounts("link_token_secret")).thenReturn(List.of(accountResponse("acc_123")));
        when(bankAccountRepository.findByUserAndBankConnectionAndExternalId(user, connection, "acc_123"))
            .thenReturn(Optional.of(account));
        when(bankAccountRepository.save(account)).thenReturn(account);
        when(bankProviderClient.listMovements(
            eq("link_token_secret"),
            eq("acc_123"),
            any(LocalDate.class),
            isNull(),
            eq(1),
            eq(300),
            eq(true)
        )).thenReturn(List.of(movement("mov_existing", -5000L, "Cafe", "CLP")));
        when(transactionRepository.existsByBankAccountAndSourceAndExternalId(account, TransactionSource.FINTOC, "mov_existing"))
            .thenReturn(true);

        FintocSyncResult result = service.syncInitial(user, connection);

        verify(transactionRepository, never()).save(any(Transaction.class));
        assertThat(result.importedTransactionsCount()).isZero();
        assertThat(result.skippedTransactionsCount()).isEqualTo(1);
    }

    @Test
    void syncSkipsDuplicateMovementReturnedInSameRun() {
        User user = user();
        BankConnection connection = connection(user);
        BankAccount account = account(user, connection);
        when(bankProviderClient.listAccounts("link_token_secret")).thenReturn(List.of(accountResponse("acc_123")));
        when(bankAccountRepository.findByUserAndBankConnectionAndExternalId(user, connection, "acc_123"))
            .thenReturn(Optional.of(account));
        when(bankAccountRepository.save(account)).thenReturn(account);
        when(bankProviderClient.listMovements(
            eq("link_token_secret"),
            eq("acc_123"),
            any(LocalDate.class),
            isNull(),
            eq(1),
            eq(300),
            eq(true)
        )).thenReturn(List.of(
            movement("mov_repeated", -5000L, "Cafe", "CLP"),
            movement("mov_repeated", -5000L, "Cafe", "CLP")
        ));
        when(transactionRepository.existsByBankAccountAndSourceAndExternalId(account, TransactionSource.FINTOC, "mov_repeated"))
            .thenReturn(false);

        FintocSyncResult result = service.syncInitial(user, connection);

        verify(transactionRepository).save(any(Transaction.class));
        assertThat(result.importedTransactionsCount()).isEqualTo(1);
        assertThat(result.skippedTransactionsCount()).isEqualTo(1);
    }

    @Test
    void syncPaginatesUntilEmptyPage() {
        User user = user();
        BankConnection connection = connection(user);
        BankAccount account = account(user, connection);
        List<FintocMovementResponse> firstPage = IntStream.range(0, 300)
            .mapToObj(index -> movement("mov_" + index, -1000L, "Movimiento " + index, "CLP"))
            .toList();
        when(bankProviderClient.listAccounts("link_token_secret")).thenReturn(List.of(accountResponse("acc_123")));
        when(bankAccountRepository.findByUserAndBankConnectionAndExternalId(user, connection, "acc_123"))
            .thenReturn(Optional.of(account));
        when(bankAccountRepository.save(account)).thenReturn(account);
        when(bankProviderClient.listMovements(
            eq("link_token_secret"),
            eq("acc_123"),
            any(LocalDate.class),
            isNull(),
            eq(1),
            eq(300),
            eq(true)
        )).thenReturn(firstPage);
        when(bankProviderClient.listMovements(
            eq("link_token_secret"),
            eq("acc_123"),
            any(LocalDate.class),
            isNull(),
            eq(2),
            eq(300),
            eq(true)
        )).thenReturn(List.of());
        when(transactionRepository.existsByBankAccountAndSourceAndExternalId(eq(account), eq(TransactionSource.FINTOC), any()))
            .thenReturn(false);

        FintocSyncResult result = service.syncInitial(user, connection);

        verify(bankProviderClient).listMovements(
            eq("link_token_secret"),
            eq("acc_123"),
            any(LocalDate.class),
            isNull(),
            eq(2),
            eq(300),
            eq(true)
        );
        assertThat(result.importedTransactionsCount()).isEqualTo(300);
        assertThat(result.skippedTransactionsCount()).isZero();
    }

    @Test
    void syncFailureDoesNotExposeTokensInExceptionMessage() {
        User user = user();
        BankConnection connection = connection(user);
        BankAccount account = account(user, connection);
        when(bankProviderClient.listAccounts("link_token_secret")).thenReturn(List.of(accountResponse("acc_123")));
        when(bankAccountRepository.findByUserAndBankConnectionAndExternalId(user, connection, "acc_123"))
            .thenReturn(Optional.of(account));
        when(bankAccountRepository.save(account)).thenReturn(account);
        when(bankProviderClient.listMovements(
            eq("link_token_secret"),
            eq("acc_123"),
            any(LocalDate.class),
            isNull(),
            eq(1),
            eq(300),
            eq(true)
        )).thenThrow(new FintocClientException("Fintoc request failed while listing movements."));

        assertThatThrownBy(() -> service.syncInitial(user, connection))
            .isInstanceOf(FintocClientException.class)
            .hasMessage("Fintoc request failed while listing movements.")
            .message()
            .doesNotContain("link_token_secret");
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

    private BankConnection connection(User user) {
        BankConnection connection = new BankConnection();
        connection.setUser(user);
        connection.setProvider(BankProvider.FINTOC);
        connection.setProviderConnectionId("link_123");
        connection.setAccessTokenRef(tokenCrypto.encrypt("link_token_secret"));
        return connection;
    }

    private BankAccount account(User user, BankConnection connection) {
        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setBankConnection(connection);
        account.setExternalId("acc_123");
        account.setName("Cuenta Vista");
        account.setAccountType("sight_account");
        account.setCurrency("CLP");
        return account;
    }

    private FintocAccountResponse accountResponse(String id) {
        return new FintocAccountResponse(
            id,
            "account",
            "Cuenta Vista",
            "CuentaRUT",
            "123456789",
            "11111111-1",
            "Persona AppSueldo",
            "sight_account",
            "CLP",
            new FintocBalanceResponse(null, 5000L, null),
            null,
            null,
            false,
            "succeeded"
        );
    }

    private FintocMovementResponse movement(String id, Long amount, String description, String currency) {
        return new FintocMovementResponse(
            id,
            "movement",
            amount,
            OffsetDateTime.parse("2026-05-25T12:30:00Z"),
            description,
            OffsetDateTime.parse("2026-05-24T12:30:00Z"),
            currency,
            "ref_" + id,
            "other",
            false,
            "posted",
            null,
            null,
            null
        );
    }
}
