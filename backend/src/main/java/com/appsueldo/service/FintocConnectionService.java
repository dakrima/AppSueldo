package com.appsueldo.service;

import com.appsueldo.config.FintocProperties;
import com.appsueldo.dto.BankAccountSummaryDto;
import com.appsueldo.dto.BankConnectionResponse;
import com.appsueldo.dto.CreateFintocLinkIntentResponse;
import com.appsueldo.dto.ExchangeFintocTokenRequest;
import com.appsueldo.dto.fintoc.FintocAccountResponse;
import com.appsueldo.dto.fintoc.FintocBalanceResponse;
import com.appsueldo.dto.fintoc.FintocLinkIntentRequest;
import com.appsueldo.dto.fintoc.FintocLinkIntentResponse;
import com.appsueldo.dto.fintoc.FintocLinkResponse;
import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.BankConnectionStatus;
import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.User;
import com.appsueldo.exception.BadRequestException;
import com.appsueldo.repository.BankAccountRepository;
import com.appsueldo.repository.BankConnectionRepository;
import com.appsueldo.service.bankprovider.BankProviderClient;
import com.appsueldo.service.fintoc.FintocClientException;
import com.appsueldo.service.fintoc.FintocTokenCrypto;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FintocConnectionService {

    private static final String DEFAULT_FINTOC_ACCOUNT_NAME = "Cuenta Fintoc";

    private final BankProviderClient bankProviderClient;
    private final FintocTokenCrypto fintocTokenCrypto;
    private final FintocProperties fintocProperties;
    private final CurrentUserService currentUserService;
    private final BankConnectionRepository bankConnectionRepository;
    private final BankAccountRepository bankAccountRepository;

    public FintocConnectionService(
        BankProviderClient bankProviderClient,
        FintocTokenCrypto fintocTokenCrypto,
        FintocProperties fintocProperties,
        CurrentUserService currentUserService,
        BankConnectionRepository bankConnectionRepository,
        BankAccountRepository bankAccountRepository
    ) {
        this.bankProviderClient = bankProviderClient;
        this.fintocTokenCrypto = fintocTokenCrypto;
        this.fintocProperties = fintocProperties;
        this.currentUserService = currentUserService;
        this.bankConnectionRepository = bankConnectionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Transactional
    public CreateFintocLinkIntentResponse createLinkIntentForCurrentUser() {
        currentUserService.currentUser();

        FintocLinkIntentResponse linkIntent = bankProviderClient.createLinkIntent(
            FintocLinkIntentRequest.movementsChileIndividual()
        );

        return new CreateFintocLinkIntentResponse(
            BankProvider.FINTOC,
            fintocProperties.publicKey(),
            requireNotBlank(linkIntent.widgetToken(), "Fintoc did not return widget token."),
            linkIntent.country(),
            linkIntent.product()
        );
    }

    @Transactional
    public BankConnectionResponse exchangeForCurrentUser(ExchangeFintocTokenRequest request) {
        String exchangeToken = request == null ? null : request.exchangeToken();
        if (exchangeToken == null || exchangeToken.isBlank()) {
            throw new BadRequestException("exchangeToken es requerido.");
        }

        User user = currentUserService.currentUser();
        FintocLinkResponse link = bankProviderClient.exchangeToken(exchangeToken.trim());
        String providerConnectionId = requireNotBlank(link.id(), "Fintoc did not return link id.");
        String linkToken = requireNotBlank(link.linkToken(), "Fintoc did not return link token.");

        BankConnection connection = bankConnectionRepository
            .findByUserAndProviderAndProviderConnectionId(user, BankProvider.FINTOC, providerConnectionId)
            .orElseGet(() -> newFintocConnection(user, providerConnectionId));

        connection.setInstitutionName(institutionName(link));
        connection.setStatus(mapStatus(link.status()));
        connection.setAccessTokenRef(fintocTokenCrypto.encrypt(linkToken));
        BankConnection savedConnection = bankConnectionRepository.save(connection);

        String decryptedLinkToken = fintocTokenCrypto.decrypt(savedConnection.getAccessTokenRef());
        List<BankAccount> savedAccounts = bankProviderClient.listAccounts(decryptedLinkToken).stream()
            .map(account -> upsertAccount(user, savedConnection, account))
            .toList();

        return BankConnectionResponse.from(
            savedConnection,
            savedAccounts.stream().map(BankAccountSummaryDto::from).toList()
        );
    }

    private BankConnection newFintocConnection(User user, String providerConnectionId) {
        BankConnection connection = new BankConnection();
        connection.setUser(user);
        connection.setProvider(BankProvider.FINTOC);
        connection.setProviderConnectionId(providerConnectionId);
        return connection;
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

    private String institutionName(FintocLinkResponse link) {
        if (link.institution() == null || link.institution().name() == null || link.institution().name().isBlank()) {
            return null;
        }
        return link.institution().name().trim();
    }

    private BankConnectionStatus mapStatus(String status) {
        if (status == null || status.isBlank()) {
            return BankConnectionStatus.PENDING;
        }
        if ("active".equalsIgnoreCase(status.trim())) {
            return BankConnectionStatus.ACTIVE;
        }
        return BankConnectionStatus.PENDING;
    }

    private String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new FintocClientException(message);
        }
        return value.trim();
    }
}
