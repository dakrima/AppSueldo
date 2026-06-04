package com.appsueldo.dto.fintoc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record FintocRefreshIntentResponse(
    String id,
    String object,
    @JsonProperty("refreshed_object") String refreshedObject,
    @JsonProperty("refreshed_object_id") String refreshedObjectId,
    String status,
    @JsonProperty("public_error") String publicError,
    @JsonProperty("created_at") OffsetDateTime createdAt,
    String type,
    @JsonProperty("new_movements") Integer newMovements,
    @JsonProperty("requires_mfa") FintocRefreshIntentMfaResponse requiresMfa
) {
}
