package com.appsueldo.dto;

public record WebhookAcceptedResponse(String status) {
    public static WebhookAcceptedResponse accepted() {
        return new WebhookAcceptedResponse("accepted");
    }
}
