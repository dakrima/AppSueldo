package com.appsueldo.dto.fintoc;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FintocLinkIntentResponse(
    String id,
    String object,
    String status,
    String product,
    String country,
    @JsonProperty("holder_type") String holderType,
    @JsonProperty("widget_token") String widgetToken,
    @JsonProperty("exchange_token") String exchangeToken
) {
    @Override
    public String toString() {
        return "FintocLinkIntentResponse[id=" + id
            + ", object=" + object
            + ", status=" + status
            + ", product=" + product
            + ", country=" + country
            + ", holderType=" + holderType
            + ", widgetToken=" + mask(widgetToken)
            + ", exchangeToken=" + mask(exchangeToken) + "]";
    }

    private static String mask(String value) {
        return value == null || value.isBlank() ? "" : "****";
    }
}
