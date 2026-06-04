package com.appsueldo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appsueldo.dto.AssignTransactionCategoryRequest;
import com.appsueldo.dto.TransactionRequest;
import com.appsueldo.dto.TransactionResponse;
import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.Category;
import com.appsueldo.entity.CategoryType;
import com.appsueldo.entity.ClassificationMethod;
import com.appsueldo.entity.Transaction;
import com.appsueldo.entity.TransactionClassification;
import com.appsueldo.entity.TransactionSource;
import com.appsueldo.entity.TransactionType;
import com.appsueldo.entity.User;
import com.appsueldo.exception.BadRequestException;
import com.appsueldo.exception.ResourceNotFoundException;
import com.appsueldo.repository.BankAccountRepository;
import com.appsueldo.repository.CategoryRepository;
import com.appsueldo.repository.TransactionClassificationRepository;
import com.appsueldo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionClassificationRepository transactionClassificationRepository;

    @Test
    void listCurrentUserTransactionsUsesAuthenticatedUserOnly() {
        User user = user("test@appsueldo.local");
        Transaction transaction = transaction(user, manualAccount(user), "Supermercado");
        TransactionService service = service(user, manualAccount(user));
        when(transactionRepository.findByUserOrderByTransactionDateDescIdDesc(user)).thenReturn(List.of(transaction));

        List<TransactionResponse> response = service.listCurrentUserTransactions();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).description()).isEqualTo("Supermercado");
        assertThat(response.get(0).bankAccount()).isNotNull();
        verify(transactionRepository).findByUserOrderByTransactionDateDescIdDesc(user);
    }

    @Test
    void createManualTransactionAssociatesManualBankAccountAndKeepsDefaults() {
        User user = user("test@appsueldo.local");
        BankAccount manualAccount = manualAccount(user);
        TransactionService service = service(user, manualAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(new TransactionRequest(
            null,
            null,
            new BigDecimal("12990.00"),
            null,
            "Supermercado",
            LocalDate.of(2026, 6, 4),
            TransactionType.EXPENSE,
            null,
            null
        ));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction savedTransaction = captor.getValue();
        assertThat(savedTransaction.getUser()).isSameAs(user);
        assertThat(savedTransaction.getBankAccount()).isSameAs(manualAccount);
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo("12990.00");
        assertThat(savedTransaction.getCurrency()).isEqualTo("CLP");
        assertThat(savedTransaction.getSource()).isEqualTo(TransactionSource.MANUAL);
        assertThat(savedTransaction.getType()).isEqualTo(TransactionType.EXPENSE);
    }

    @Test
    void createManualTransactionUsesOwnedBankAccountWhenProvided() {
        User user = user("test@appsueldo.local");
        BankAccount account = manualAccount(user);
        setId(account, 77L);
        TransactionService service = service(user, manualAccount(user));
        when(bankAccountRepository.findByIdAndUser(77L, user)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(new TransactionRequest(
            77L,
            null,
            new BigDecimal("25000.00"),
            "usd",
            "Ingreso freelance",
            LocalDate.of(2026, 6, 5),
            TransactionType.INCOME,
            TransactionSource.MANUAL,
            "Pago parcial"
        ));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getBankAccount()).isSameAs(account);
        assertThat(captor.getValue().getCurrency()).isEqualTo("USD");
        assertThat(captor.getValue().getSource()).isEqualTo(TransactionSource.MANUAL);
    }

    @Test
    void createManualTransactionRejectsAnotherUsersBankAccount() {
        User user = user("test@appsueldo.local");
        TransactionService service = service(user, manualAccount(user));
        when(bankAccountRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new TransactionRequest(
            99L,
            null,
            new BigDecimal("5000.00"),
            null,
            "Cafe",
            LocalDate.of(2026, 6, 6),
            TransactionType.EXPENSE,
            null,
            null
        ))).isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Cuenta bancaria no encontrada.");

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createManualTransactionRejectsFintocSource() {
        User user = user("test@appsueldo.local");
        TransactionService service = service(user, manualAccount(user));

        assertThatThrownBy(() -> service.create(new TransactionRequest(
            null,
            null,
            new BigDecimal("5000.00"),
            null,
            "Cafe",
            LocalDate.of(2026, 6, 6),
            TransactionType.EXPENSE,
            TransactionSource.FINTOC,
            null
        ))).isInstanceOf(BadRequestException.class)
            .hasMessage("No se pueden crear transacciones Fintoc manualmente.");
    }

    @Test
    void assignCategoryUpdatesTransactionAndCreatesManualClassification() {
        User user = user("test@appsueldo.local");
        Transaction transaction = transaction(user, manualAccount(user), "Supermercado");
        Category category = category(user, "Alimentacion");
        TransactionService service = service(user, manualAccount(user));
        when(transactionRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(transaction));
        when(categoryRepository.findByIdAndUser(20L, user)).thenReturn(Optional.of(category));
        when(transactionClassificationRepository.findByTransactionAndUser(transaction, user)).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.assignCategory(10L, new AssignTransactionCategoryRequest(20L));

        assertThat(transaction.getCategory()).isSameAs(category);
        ArgumentCaptor<TransactionClassification> captor = ArgumentCaptor.forClass(TransactionClassification.class);
        verify(transactionClassificationRepository).save(captor.capture());
        TransactionClassification classification = captor.getValue();
        assertThat(classification.getUser()).isSameAs(user);
        assertThat(classification.getTransaction()).isSameAs(transaction);
        assertThat(classification.getCategory()).isSameAs(category);
        assertThat(classification.getMethod()).isEqualTo(ClassificationMethod.MANUAL);
        assertThat(classification.getReason()).isEqualTo("Categoria asignada manualmente.");
    }

    @Test
    void assignCategoryRejectsAnotherUsersCategory() {
        User user = user("test@appsueldo.local");
        Transaction transaction = transaction(user, manualAccount(user), "Supermercado");
        TransactionService service = service(user, manualAccount(user));
        when(transactionRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(transaction));
        when(categoryRepository.findByIdAndUser(20L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignCategory(10L, new AssignTransactionCategoryRequest(20L)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Categoria no encontrada.");

        verify(transactionClassificationRepository, never()).save(any(TransactionClassification.class));
    }

    @Test
    void assignCategoryAllowsRemovingCategory() {
        User user = user("test@appsueldo.local");
        BankAccount account = manualAccount(user);
        Transaction transaction = transaction(user, account, "Supermercado");
        transaction.setCategory(category(user, "Alimentacion"));
        TransactionClassification classification = new TransactionClassification();
        TransactionService service = service(user, account);
        when(transactionRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(transaction));
        when(transactionClassificationRepository.findByTransactionAndUser(transaction, user))
            .thenReturn(Optional.of(classification));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.assignCategory(10L, new AssignTransactionCategoryRequest(null));

        assertThat(transaction.getCategory()).isNull();
        assertThat(classification.getCategory()).isNull();
        assertThat(classification.getMethod()).isEqualTo(ClassificationMethod.MANUAL);
        assertThat(classification.getReason()).isEqualTo("Categoria quitada manualmente.");
    }

    private TransactionService service(User user, BankAccount manualAccount) {
        return new TransactionService(
            transactionRepository,
            categoryRepository,
            bankAccountRepository,
            transactionClassificationRepository,
            new FakeCurrentUserService(user),
            new FakeBankAccountService(manualAccount)
        );
    }

    private User user(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        return user;
    }

    private Category category(User user, String name) {
        Category category = new Category();
        category.setUser(user);
        category.setName(name);
        category.setType(CategoryType.EXPENSE);
        return category;
    }

    private Transaction transaction(User user, BankAccount account, String description) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setBankAccount(account);
        transaction.setAmount(new BigDecimal("10000.00"));
        transaction.setCurrency("CLP");
        transaction.setDescription(description);
        transaction.setTransactionDate(LocalDate.of(2026, 6, 4));
        transaction.setType(TransactionType.EXPENSE);
        transaction.setSource(TransactionSource.MANUAL);
        return transaction;
    }

    private BankAccount manualAccount(User user) {
        BankConnection connection = new BankConnection();
        connection.setUser(user);
        connection.setProvider(BankProvider.MANUAL);

        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setBankConnection(connection);
        account.setName("Cuenta manual");
        account.setAccountType("MANUAL");
        return account;
    }

    private void setId(BankAccount bankAccount, Long id) {
        try {
            var field = BankAccount.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(bankAccount, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not set bank account id for test.", exception);
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
