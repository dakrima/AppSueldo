package com.appsueldo.dto.fintoc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record FintocWebhookEventData(
    String object,
    @JsonProperty("refreshed_object") String refreshedObject,
    @JsonProperty("refreshed_object_id") String refreshedObjectId,
    String status,
    @JsonProperty("public_error") String publicError,
    @JsonProperty("created_at") OffsetDateTime createdAt,
    String type,
    @JsonProperty("new_movements") Integer newMovements
) {
}
