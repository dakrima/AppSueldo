package com.appsueldo.controller;

import com.appsueldo.dto.BankConnectionResponse;
import com.appsueldo.service.BankConnectionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bank-connections")
public class BankConnectionController {

    private final BankConnectionService bankConnectionService;

    public BankConnectionController(BankConnectionService bankConnectionService) {
        this.bankConnectionService = bankConnectionService;
    }

    @GetMapping
    public List<BankConnectionResponse> list() {
        return bankConnectionService.listCurrentUserConnections();
    }
}
