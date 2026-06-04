package com.appsueldo.dto.fintoc;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FintocMovementAccountResponse(
    @JsonProperty("holder_id") String holderId,
    @JsonProperty("holder_name") String holderName,
    String number,
    FintocInstitutionResponse institution
) {
    @Override
    public String toString() {
        return "FintocMovementAccountResponse[holderId=" + mask(holderId)
            + ", holderName=" + holderName
            + ", number=" + mask(number)
            + ", institution=" + institution + "]";
    }

    private static String mask(String value) {
        return value == null || value.isBlank() ? "" : "****";
    }
}
