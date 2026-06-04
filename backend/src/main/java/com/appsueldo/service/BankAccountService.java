package com.appsueldo.service;

import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.BankConnectionStatus;
import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.User;
import com.appsueldo.repository.BankAccountRepository;
import com.appsueldo.repository.BankConnectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankAccountService {

    private static final String MANUAL_INSTITUTION_NAME = "Manual";
    private static final String MANUAL_ACCOUNT_NAME = "Cuenta manual";
    private static final String MANUAL_ACCOUNT_TYPE = "MANUAL";

    private final BankConnectionRepository bankConnectionRepository;
    private final BankAccountRepository bankAccountRepository;

    public BankAccountService(
        BankConnectionRepository bankConnectionRepository,
        BankAccountRepository bankAccountRepository
    ) {
        this.bankConnectionRepository = bankConnectionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Transactional
    public BankAccount getOrCreateManualAccount(User user) {
        return bankAccountRepository.findFirstByUserAndBankConnectionProviderOrderByIdAsc(user, BankProvider.MANUAL)
            .orElseGet(() -> createManualAccount(user));
    }

    private BankAccount createManualAccount(User user) {
        BankConnection bankConnection = bankConnectionRepository.findFirstByUserAndProviderOrderByIdAsc(
            user,
            BankProvider.MANUAL
        ).orElseGet(() -> createManualConnection(user));

        BankAccount bankAccount = new BankAccount();
        bankAccount.setUser(user);
        bankAccount.setBankConnection(bankConnection);
        bankAccount.setName(MANUAL_ACCOUNT_NAME);
        bankAccount.setAccountType(MANUAL_ACCOUNT_TYPE);
        bankAccount.setCurrency(BankAccount.DEFAULT_CURRENCY);
        return bankAccountRepository.save(bankAccount);
    }

    private BankConnection createManualConnection(User user) {
        BankConnection bankConnection = new BankConnection();
        bankConnection.setUser(user);
        bankConnection.setProvider(BankProvider.MANUAL);
        bankConnection.setInstitutionName(MANUAL_INSTITUTION_NAME);
        bankConnection.setStatus(BankConnectionStatus.ACTIVE);
        return bankConnectionRepository.save(bankConnection);
    }
}
