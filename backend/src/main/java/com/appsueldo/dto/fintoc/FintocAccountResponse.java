package com.appsueldo.dto.fintoc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record FintocAccountResponse(
    String id,
    String object,
    String name,
    @JsonProperty("official_name") String officialName,
    String number,
    @JsonProperty("holder_id") String holderId,
    @JsonProperty("holder_name") String holderName,
    String type,
    String currency,
    FintocBalanceResponse balance,
    @JsonProperty("refreshed_at") OffsetDateTime refreshedAt,
    @JsonProperty("next_refresh") OffsetDateTime nextRefresh,
    @JsonProperty("removed_from_link") Boolean removedFromLink,
    @JsonProperty("refresh_status") String refreshStatus
) {
    @Override
    public String toString() {
        return "FintocAccountResponse[id=" + id
            + ", object=" + object
            + ", name=" + name
            + ", officialName=" + officialName
            + ", number=" + mask(number)
            + ", holderId=" + mask(holderId)
            + ", holderName=" + holderName
            + ", type=" + type
            + ", currency=" + currency
            + ", balance=" + balance
            + ", refreshedAt=" + refreshedAt
            + ", nextRefresh=" + nextRefresh
            + ", removedFromLink=" + removedFromLink
            + ", refreshStatus=" + refreshStatus + "]";
    }

    private static String mask(String value) {
        return value == null || value.isBlank() ? "" : "****";
    }
}
