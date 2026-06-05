package com.appsueldo.dto.fintoc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record FintocWebhookEventPayload(
    String id,
    String type,
    String mode,
    @JsonProperty("created_at") OffsetDateTime createdAt,
    FintocWebhookEventData data,
    String object
) {
}
