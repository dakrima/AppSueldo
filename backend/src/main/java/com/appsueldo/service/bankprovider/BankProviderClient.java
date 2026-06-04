package com.appsueldo.service.bankprovider;

import com.appsueldo.dto.fintoc.FintocAccountResponse;
import com.appsueldo.dto.fintoc.FintocLinkIntentRequest;
import com.appsueldo.dto.fintoc.FintocLinkIntentResponse;
import com.appsueldo.dto.fintoc.FintocLinkResponse;
import com.appsueldo.dto.fintoc.FintocMovementResponse;
import com.appsueldo.dto.fintoc.FintocRefreshIntentResponse;
import java.time.LocalDate;
import java.util.List;

public interface BankProviderClient {
    FintocLinkIntentResponse createLinkIntent(FintocLinkIntentRequest request);

    FintocLinkResponse exchangeToken(String exchangeToken);

    List<FintocAccountResponse> listAccounts(String linkToken);

    List<FintocMovementResponse> listMovements(
        String linkToken,
        String accountId,
        LocalDate since,
        LocalDate until,
        int page,
        int perPage,
        boolean confirmedOnly
    );

    FintocRefreshIntentResponse createRefreshIntent(String linkToken, String refreshType);
}
