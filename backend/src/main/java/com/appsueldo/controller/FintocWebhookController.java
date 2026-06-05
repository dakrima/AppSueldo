package com.appsueldo.controller;

import com.appsueldo.dto.WebhookAcceptedResponse;
import com.appsueldo.service.FintocWebhookService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/fintoc")
public class FintocWebhookController {

    private final FintocWebhookService fintocWebhookService;

    public FintocWebhookController(FintocWebhookService fintocWebhookService) {
        this.fintocWebhookService = fintocWebhookService;
    }

    @PostMapping
    public WebhookAcceptedResponse handle(
        @RequestBody String rawBody,
        @RequestHeader(name = "Fintoc-Signature", required = false) String signature
    ) {
        fintocWebhookService.handle(rawBody, signature);
        return WebhookAcceptedResponse.accepted();
    }
}
