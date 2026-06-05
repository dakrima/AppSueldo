package com.appsueldo.service;

import com.appsueldo.config.FintocProperties;
import com.appsueldo.dto.fintoc.FintocAccountResponse;
import com.appsueldo.dto.fintoc.FintocBalanceResponse;
import com.appsueldo.dto.fintoc.FintocMovementResponse;
import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.Transaction;
import com.appsueldo.entity.TransactionSource;
import com.appsueldo.entity.User;
import com.appsueldo.exception.BadRequestException;
import com.appsueldo.repository.BankAccountRepository;
import com.appsueldo.repository.TransactionRepository;
import com.appsueldo.service.bankprovider.BankProviderClient;
import com.appsueldo.service.fintoc.FintocClientException;
import com.appsueldo.service.fintoc.FintocMovementMapper;
import com.appsueldo.service.fintoc.FintocTokenCrypto;
import com.appsueldo.service.fintoc.FintocTransactionDraft;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FintocSyncService {

    private static final String DEFAULT_FINTOC_ACCOUNT_NAME = "Cuenta Fintoc";
    private static final int MOVEMENTS_PER_PAGE = 300;
    private static final int MAX_MOVEMENT_PAGES = 100;

    private final BankProviderClient bankProviderClient;
    private final FintocTokenCrypto fintocTokenCrypto;
    private final FintocMovementMapper fintocMovementMapper;
    private final FintocProperties fintocProperties;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    public FintocSyncService(
        BankProviderClient bankProviderClient,
        FintocTokenCrypto fintocTokenCrypto,
        FintocMovementMapper fintocMovementMapper,
        FintocProperties fintocProperties,
        BankAccountRepository bankAccountRepository,
        TransactionRepository transactionRepository
    ) {
        this.bankProviderClient = bankProviderClient;
        this.fintocTokenCrypto = fintocTokenCrypto;
        this.fintocMovementMapper = fintocMovementMapper;
        this.fintocProperties = fintocProperties;
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public FintocSyncResult syncInitial(User user, BankConnection connection) {
        validateConnectionOwnership(user, connection);
        String linkToken = fintocTokenCrypto.decrypt(
            requireNotBlank(connection.getAccessTokenRef(), "Fintoc connection token is missing.")
        );
        LocalDate since = LocalDate.now().minusDays(fintocProperties.initialSyncDays());

        List<BankAccount> accounts = bankProviderClient.listAccounts(linkToken).stream()
            .map(account -> upsertAccount(user, connection, account))
            .toList();

        int importedTransactionsCount = 0;
        int skippedTransactionsCount = 0;
        for (BankAccount account : accounts) {
            MovementSyncCounts counts = syncAccountMovements(user, account, linkToken, since);
            importedTransactionsCount += counts.importedTransactionsCount();
            skippedTransactionsCount += counts.skippedTransactionsCount();
        }

        return FintocSyncResult.completed(importedTransactionsCount, skippedTransactionsCount, accounts);
    }

    private MovementSyncCounts syncAccountMovements(
        User user,
        BankAccount account,
        String linkToken,
        LocalDate since
    ) {
        int importedTransactionsCount = 0;
        int skippedTransactionsCount = 0;
        Set<String> seenExternalIds = new HashSet<>();

        for (int page = 1; page <= MAX_MOVEMENT_PAGES; page++) {
            List<FintocMovementResponse> movements = bankProviderClient.listMovements(
                linkToken,
                requireNotBlank(account.getExternalId(), "Fintoc account id is missing."),
                since,
                null,
                page,
                MOVEMENTS_PER_PAGE,
                true
            );

            if (movements.isEmpty()) {
                break;
            }

            for (FintocMovementResponse movement : movements) {
                FintocTransactionDraft draft = fintocMovementMapper.toTransactionDraft(movement);
                if (!seenExternalIds.add(draft.externalId()) || alreadyImported(account, draft.externalId())) {
                    skippedTransactionsCount++;
                    continue;
                }
                transactionRepository.save(toTransaction(user, account, draft));
                importedTransactionsCount++;
            }

            if (movements.size() < MOVEMENTS_PER_PAGE) {
                break;
            }
        }

        return new MovementSyncCounts(importedTransactionsCount, skippedTransactionsCount);
    }

    private boolean alreadyImported(BankAccount account, String externalId) {
        return transactionRepository.existsByBankAccountAndSourceAndExternalId(
            account,
            TransactionSource.FINTOC,
            externalId
        );
    }

    private Transaction toTransaction(User user, BankAccount bankAccount, FintocTransactionDraft draft) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setBankAccount(bankAccount);
        transaction.setAmount(draft.amount());
        transaction.setCurrency(draft.currency());
        transaction.setDescription(draft.description());
        transaction.setTransactionDate(draft.transactionDate());
        transaction.setType(draft.type());
        transaction.setSource(TransactionSource.FINTOC);
        transaction.setExternalId(draft.externalId());
        return transaction;
    }

    private BankAccount upsertAccount(
        User user,
        BankConnection connection,
        FintocAccountResponse accountResponse
    ) {
        String externalId = requireNotBlank(accountResponse.id(), "Fintoc did not return account id.");
        BankAccount account = bankAccountRepository
            .findByUserAndBankConnectionAndExternalId(user, connection, externalId)
            .orElseGet(() -> newFintocAccount(user, connection, externalId));

        account.setName(accountName(accountResponse));
        account.setAccountType(accountResponse.type());
        account.setCurrency(accountResponse.currency());
        account.setBalance(balance(accountResponse.balance()));
        return bankAccountRepository.save(account);
    }

    private BankAccount newFintocAccount(User user, BankConnection connection, String externalId) {
        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setBankConnection(connection);
        account.setExternalId(externalId);
        return account;
    }

    private String accountName(FintocAccountResponse accountResponse) {
        if (accountResponse.name() != null && !accountResponse.name().isBlank()) {
            return accountResponse.name().trim();
        }
        if (accountResponse.officialName() != null && !accountResponse.officialName().isBlank()) {
            return accountResponse.officialName().trim();
        }
        return DEFAULT_FINTOC_ACCOUNT_NAME;
    }

    private BigDecimal balance(FintocBalanceResponse balance) {
        if (balance == null) {
            return null;
        }
        if (balance.current() != null) {
            return BigDecimal.valueOf(balance.current());
        }
        if (balance.available() != null) {
            return BigDecimal.valueOf(balance.available());
        }
        return null;
    }

    private void validateConnectionOwnership(User user, BankConnection connection) {
        if (connection == null || connection.getProvider() != BankProvider.FINTOC) {
            throw new BadRequestException("Conexion Fintoc invalida.");
        }
        if (!sameUser(user, connection.getUser())) {
            throw new BadRequestException("Conexion Fintoc invalida.");
        }
    }

    private boolean sameUser(User expected, User actual) {
        if (expected == null || actual == null) {
            return false;
        }
        if (expected.getId() != null && actual.getId() != null) {
            return expected.getId().equals(actual.getId());
        }
        return expected == actual;
    }

    private String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new FintocClientException(message);
        }
        return value.trim();
    }

    private record MovementSyncCounts(
        int importedTransactionsCount,
        int skippedTransactionsCount
    ) {
    }
}
