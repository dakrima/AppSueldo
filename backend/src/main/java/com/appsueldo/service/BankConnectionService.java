package com.appsueldo.service;

import com.appsueldo.dto.BankAccountSummaryDto;
import com.appsueldo.dto.BankConnectionResponse;
import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.User;
import com.appsueldo.repository.BankAccountRepository;
import com.appsueldo.repository.BankConnectionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankConnectionService {

    private final BankConnectionRepository bankConnectionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankAccountService bankAccountService;
    private final CurrentUserService currentUserService;

    public BankConnectionService(
        BankConnectionRepository bankConnectionRepository,
        BankAccountRepository bankAccountRepository,
        BankAccountService bankAccountService,
        CurrentUserService currentUserService
    ) {
        this.bankConnectionRepository = bankConnectionRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.bankAccountService = bankAccountService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public List<BankConnectionResponse> listCurrentUserConnections() {
        User user = currentUserService.currentUser();
        bankAccountService.getOrCreateManualAccount(user);

        List<BankAccount> accounts = bankAccountRepository.findByUserOrderByNameAsc(user);
        return bankConnectionRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .map(connection -> BankConnectionResponse.from(connection, accountsForConnection(connection, accounts)))
            .toList();
    }

    private List<BankAccountSummaryDto> accountsForConnection(
        BankConnection bankConnection,
        List<BankAccount> accounts
    ) {
        return accounts.stream()
            .filter(account -> belongsToConnection(account, bankConnection))
            .map(BankAccountSummaryDto::from)
            .toList();
    }

    private boolean belongsToConnection(BankAccount bankAccount, BankConnection bankConnection) {
        BankConnection accountConnection = bankAccount.getBankConnection();
        if (accountConnection == null) {
            return false;
        }
        if (accountConnection.getId() != null && bankConnection.getId() != null) {
            return accountConnection.getId().equals(bankConnection.getId());
        }
        return accountConnection == bankConnection;
    }
}
