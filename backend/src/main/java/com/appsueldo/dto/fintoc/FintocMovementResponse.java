package com.appsueldo.dto.fintoc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record FintocMovementResponse(
    String id,
    String object,
    Long amount,
    @JsonProperty("post_date") OffsetDateTime postDate,
    String description,
    @JsonProperty("transaction_date") OffsetDateTime transactionDate,
    String currency,
    @JsonProperty("reference_id") String referenceId,
    String type,
    Boolean pending,
    String status,
    @JsonProperty("recipient_account") FintocMovementAccountResponse recipientAccount,
    @JsonProperty("sender_account") FintocMovementAccountResponse senderAccount,
    String comment
) {
}
