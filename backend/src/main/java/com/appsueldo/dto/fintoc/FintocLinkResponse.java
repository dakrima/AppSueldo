package com.appsueldo.dto.fintoc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FintocLinkResponse(
    String id,
    String object,
    String status,
    @JsonProperty("link_token") String linkToken,
    FintocInstitutionResponse institution,
    List<FintocAccountResponse> accounts
) {
    @Override
    public String toString() {
        return "FintocLinkResponse[id=" + id
            + ", object=" + object
            + ", status=" + status
            + ", linkToken=" + mask(linkToken)
            + ", institution=" + institution
            + ", accountsCount=" + (accounts == null ? 0 : accounts.size()) + "]";
    }

    private static String mask(String value) {
        return value == null || value.isBlank() ? "" : "****";
    }
}
