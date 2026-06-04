package com.appsueldo.dto.fintoc;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FintocRefreshIntentMfaResponse(
    @JsonProperty("widget_token") String widgetToken
) {
    @Override
    public String toString() {
        return "FintocRefreshIntentMfaResponse[widgetToken=" + (widgetToken == null ? "" : "****") + "]";
    }
}
