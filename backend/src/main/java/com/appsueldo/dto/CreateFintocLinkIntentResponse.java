package com.appsueldo.dto;

import com.appsueldo.entity.BankProvider;

public record CreateFintocLinkIntentResponse(
    BankProvider provider,
    String publicKey,
    String widgetToken,
    String country,
    String product
) {
}
