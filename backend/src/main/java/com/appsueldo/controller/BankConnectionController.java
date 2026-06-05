package com.appsueldo.controller;

import com.appsueldo.dto.BankConnectionResponse;
import com.appsueldo.dto.BankConnectionSyncResponse;
import com.appsueldo.dto.CreateFintocLinkIntentResponse;
import com.appsueldo.dto.ExchangeFintocTokenRequest;
import com.appsueldo.service.BankConnectionService;
import com.appsueldo.service.FintocConnectionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bank-connections")
public class BankConnectionController {

    private final BankConnectionService bankConnectionService;
    private final FintocConnectionService fintocConnectionService;

    public BankConnectionController(
        BankConnectionService bankConnectionService,
        FintocConnectionService fintocConnectionService
    ) {
        this.bankConnectionService = bankConnectionService;
        this.fintocConnectionService = fintocConnectionService;
    }

    @GetMapping
    public List<BankConnectionResponse> list() {
        return bankConnectionService.listCurrentUserConnections();
    }

    @PostMapping("/fintoc/link-intents")
    public CreateFintocLinkIntentResponse createFintocLinkIntent() {
        return fintocConnectionService.createLinkIntentForCurrentUser();
    }

    @PostMapping("/fintoc/exchange")
    public BankConnectionResponse exchangeFintocToken(
        @Valid @RequestBody ExchangeFintocTokenRequest request
    ) {
        return fintocConnectionService.exchangeForCurrentUser(request);
    }

    @PostMapping("/{id}/sync")
    public BankConnectionSyncResponse syncBankConnection(@PathVariable Long id) {
        return fintocConnectionService.syncConnectionForCurrentUser(id);
    }
}
