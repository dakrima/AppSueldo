package com.appsueldo.service.fintoc;

import com.appsueldo.dto.fintoc.FintocAccountResponse;
import com.appsueldo.dto.fintoc.FintocLinkIntentRequest;
import com.appsueldo.dto.fintoc.FintocLinkIntentResponse;
import com.appsueldo.dto.fintoc.FintocLinkResponse;
import com.appsueldo.dto.fintoc.FintocMovementResponse;
import com.appsueldo.dto.fintoc.FintocRefreshIntentResponse;
import com.appsueldo.service.bankprovider.BankProviderClient;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

@Component
public class FintocClient implements BankProviderClient {

    private final RestClient restClient;

    public FintocClient(@Qualifier("fintocRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public FintocLinkIntentResponse createLinkIntent(FintocLinkIntentRequest request) {
        return execute("creating link intent", () -> requireResponse(
            restClient.post()
                .uri("/v1/link_intents")
                .body(request)
                .retrieve()
                .body(FintocLinkIntentResponse.class),
            "creating link intent"
        ));
    }

    @Override
    public FintocLinkResponse exchangeToken(String exchangeToken) {
        return execute("exchanging link token", () -> requireResponse(
            restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v1/links/exchange")
                    .queryParam("exchange_token", exchangeToken)
                    .build()
                )
                .retrieve()
                .body(FintocLinkResponse.class),
            "exchanging link token"
        ));
    }

    @Override
    public List<FintocAccountResponse> listAccounts(String linkToken) {
        return execute("listing accounts", () -> asList(
            restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v1/accounts")
                    .queryParam("link_token", linkToken)
                    .build()
                )
                .retrieve()
                .body(FintocAccountResponse[].class)
        ));
    }

    @Override
    public List<FintocMovementResponse> listMovements(
        String linkToken,
        String accountId,
        LocalDate since,
        LocalDate until,
        int page,
        int perPage,
        boolean confirmedOnly
    ) {
        return execute("listing movements", () -> asList(
            restClient.get()
                .uri(uriBuilder -> movementsUri(
                    uriBuilder,
                    accountId,
                    linkToken,
                    since,
                    until,
                    page,
                    perPage,
                    confirmedOnly
                ))
                .retrieve()
                .body(FintocMovementResponse[].class)
        ));
    }

    @Override
    public FintocRefreshIntentResponse createRefreshIntent(String linkToken, String refreshType) {
        return execute("creating refresh intent", () -> requireResponse(
            restClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/v1/refresh_intents")
                    .queryParam("link_token", linkToken)
                    .queryParam("refresh_type", refreshType)
                    .build()
                )
                .retrieve()
                .body(FintocRefreshIntentResponse.class),
            "creating refresh intent"
        ));
    }

    private URI movementsUri(
        UriBuilder uriBuilder,
        String accountId,
        String linkToken,
        LocalDate since,
        LocalDate until,
        int page,
        int perPage,
        boolean confirmedOnly
    ) {
        UriBuilder builder = uriBuilder
            .path("/v1/accounts/{accountId}/movements")
            .queryParam("link_token", linkToken)
            .queryParam("page", page)
            .queryParam("per_page", perPage)
            .queryParam("confirmed_only", confirmedOnly);

        if (since != null) {
            builder.queryParam("since", since);
        }
        if (until != null) {
            builder.queryParam("until", until);
        }
        return builder.build(accountId);
    }

    private <T> T execute(String operation, Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RestClientException exception) {
            throw new FintocClientException("Fintoc request failed while " + operation + ".");
        }
    }

    private <T> T requireResponse(T response, String operation) {
        if (response == null) {
            throw new FintocClientException("Fintoc returned an empty response while " + operation + ".");
        }
        return response;
    }

    private <T> List<T> asList(T[] response) {
        if (response == null || response.length == 0) {
            return List.of();
        }
        return Arrays.asList(response);
    }
}
