package com.appsueldo.service;

import com.appsueldo.config.FintocProperties;
import com.appsueldo.dto.BankAccountSummaryDto;
import com.appsueldo.dto.BankConnectionResponse;
import com.appsueldo.dto.CreateFintocLinkIntentResponse;
import com.appsueldo.dto.ExchangeFintocTokenRequest;
import com.appsueldo.dto.fintoc.FintocLinkIntentRequest;
import com.appsueldo.dto.fintoc.FintocLinkIntentResponse;
import com.appsueldo.dto.fintoc.FintocLinkResponse;
import com.appsueldo.entity.BankConnection;
import com.appsueldo.entity.BankConnectionStatus;
import com.appsueldo.entity.BankProvider;
import com.appsueldo.entity.User;
import com.appsueldo.exception.BadRequestException;
import com.appsueldo.repository.BankConnectionRepository;
import com.appsueldo.service.bankprovider.BankProviderClient;
import com.appsueldo.service.fintoc.FintocClientException;
import com.appsueldo.service.fintoc.FintocTokenCrypto;
import org.springframework.stereotype.Service;

@Service
public class FintocConnectionService {

    private final BankProviderClient bankProviderClient;
    private final FintocTokenCrypto fintocTokenCrypto;
    private final FintocProperties fintocProperties;
    private final CurrentUserService currentUserService;
    private final BankConnectionRepository bankConnectionRepository;
    private final FintocSyncService fintocSyncService;

    public FintocConnectionService(
        BankProviderClient bankProviderClient,
        FintocTokenCrypto fintocTokenCrypto,
        FintocProperties fintocProperties,
        CurrentUserService currentUserService,
        BankConnectionRepository bankConnectionRepository,
        FintocSyncService fintocSyncService
    ) {
        this.bankProviderClient = bankProviderClient;
        this.fintocTokenCrypto = fintocTokenCrypto;
        this.fintocProperties = fintocProperties;
        this.currentUserService = currentUserService;
        this.bankConnectionRepository = bankConnectionRepository;
        this.fintocSyncService = fintocSyncService;
    }

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

        FintocSyncResult syncResult = syncInitialMovements(user, savedConnection);

        return BankConnectionResponse.from(
            savedConnection,
            syncResult.accounts().stream().map(BankAccountSummaryDto::from).toList(),
            syncResult.importedTransactionsCount(),
            syncResult.skippedTransactionsCount(),
            syncResult.syncStatus()
        );
    }

    private BankConnection newFintocConnection(User user, String providerConnectionId) {
        BankConnection connection = new BankConnection();
        connection.setUser(user);
        connection.setProvider(BankProvider.FINTOC);
        connection.setProviderConnectionId(providerConnectionId);
        return connection;
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

    private FintocSyncResult syncInitialMovements(User user, BankConnection connection) {
        try {
            return fintocSyncService.syncInitial(user, connection);
        } catch (RuntimeException exception) {
            return FintocSyncResult.failed();
        }
    }

    private String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new FintocClientException(message);
        }
        return value.trim();
    }
}
