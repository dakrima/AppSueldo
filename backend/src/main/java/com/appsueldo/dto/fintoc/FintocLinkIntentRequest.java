package com.appsueldo.dto.fintoc;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FintocLinkIntentRequest(
    String product,
    String country,
    @JsonProperty("holder_type") String holderType
) {
    public static FintocLinkIntentRequest movementsChileIndividual() {
        return new FintocLinkIntentRequest("movements", "cl", "individual");
    }
}
