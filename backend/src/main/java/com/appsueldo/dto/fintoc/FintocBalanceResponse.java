package com.appsueldo.dto.fintoc;

public record FintocBalanceResponse(
    Long available,
    Long current,
    Long limit
) {
}
