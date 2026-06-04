package com.appsueldo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appsueldo.dto.ImportBatchResponse;
import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.ImportBatch;
import com.appsueldo.entity.ImportBatchStatus;
import com.appsueldo.entity.Transaction;
import com.appsueldo.entity.TransactionSource;
import com.appsueldo.entity.TransactionType;
import com.appsueldo.entity.User;
import com.appsueldo.exception.BadRequestException;
import com.appsueldo.exception.ResourceNotFoundException;
import com.appsueldo.repository.ImportBatchRepository;
import com.appsueldo.repository.TransactionRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class TransactionImportServiceTest {

    @Mock
    private ImportBatchRepository importBatchRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void importsValidCsvRowsIntoManualAccountAndStoresBatchSummary() {
        User user = user();
        BankAccount account = account(user);
        TransactionImportService service = service(user, account);
        when(importBatchRepository.save(any(ImportBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.existsByBankAccountAndSourceAndExternalId(
            eq(account),
            eq(TransactionSource.CSV_IMPORT),
            anyString()
        ))
            .thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportBatchResponse response = service.importCurrentUserCsv(file("""
            fecha,monto,descripcion
            2026-06-02,-12500,Cafe
            fecha-invalida,0,
            """));

        assertThat(response.status()).isEqualTo(ImportBatchStatus.COMPLETED);
        assertThat(response.totalRows()).isEqualTo(2);
        assertThat(response.createdCount()).isEqualTo(1);
        assertThat(response.invalidCount()).isEqualTo(1);
        assertThat(response.skippedCount()).isZero();
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction transaction = transactionCaptor.getValue();
        assertThat(transaction.getUser()).isSameAs(user);
        assertThat(transaction.getBankAccount()).isSameAs(account);
        assertThat(transaction.getSource()).isEqualTo(TransactionSource.CSV_IMPORT);
        assertThat(transaction.getExternalId()).startsWith("csv:");
        assertThat(transaction.getAmount()).isEqualByComparingTo("12500.00");
        assertThat(transaction.getType()).isEqualTo(TransactionType.EXPENSE);
    }

    @Test
    void skipsRowsAlreadyImportedOrDuplicatedInSameFile() {
        User user = user();
        BankAccount account = account(user);
        TransactionImportService service = service(user, account);
        when(importBatchRepository.save(any(ImportBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.existsByBankAccountAndSourceAndExternalId(
            eq(account),
            eq(TransactionSource.CSV_IMPORT),
            anyString()
        )).thenReturn(true, false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportBatchResponse response = service.importCurrentUserCsv(file("""
            fecha,monto,descripcion
            2026-06-01,-1000,Movimiento existente
            2026-06-02,-2000,Movimiento nuevo
            2026-06-02,-2000,Movimiento nuevo
            """));

        assertThat(response.createdCount()).isEqualTo(1);
        assertThat(response.skippedCount()).isEqualTo(2);
        assertThat(response.invalidCount()).isZero();
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void rejectsEmptyFileBeforeCreatingBatch() throws Exception {
        User user = user();
        BankAccount account = account(user);
        TransactionImportService service = service(user, account);
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.csv",
            "text/csv",
            new byte[0]
        );

        assertThatThrownBy(() -> service.importCurrentUserCsv(emptyFile))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Debes subir un archivo CSV con movimientos.");

        verify(importBatchRepository, never()).save(any(ImportBatch.class));
    }

    @Test
    void getBatchRejectsAnotherUsersBatch() {
        User user = user();
        TransactionImportService service = service(user, account(user));
        when(importBatchRepository.findByIdAndUser(10L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentUserBatch(10L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Importacion no encontrada.");
    }

    private TransactionImportService service(User user, BankAccount account) {
        return new TransactionImportService(
            importBatchRepository,
            transactionRepository,
            new FakeCurrentUserService(user),
            new FakeBankAccountService(account),
            new TransactionCsvParser()
        );
    }

    private MockMultipartFile file() {
        return file("fecha,monto,descripcion\n2026-06-01,-12500,Cafe\n");
    }

    private MockMultipartFile file(String content) {
        return new MockMultipartFile(
            "file",
            "movimientos.csv",
            "text/csv",
            (content.stripIndent().trim() + "\n").getBytes()
        );
    }

    private User user() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@appsueldo.local");
        return user;
    }

    private BankAccount account(User user) {
        BankConnection connection = new BankConnection();
        connection.setUser(user);
        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setBankConnection(connection);
        account.setName("Cuenta manual");
        account.setCurrency("CLP");
        return account;
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
