package com.appsueldo.dto;

import jakarta.validation.constraints.NotBlank;

public record ExchangeFintocTokenRequest(
    @NotBlank String exchangeToken
) {
}
