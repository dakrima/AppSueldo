package com.appsueldo.dto.fintoc;

import static org.assertj.core.api.Assertions.assertThat;

import com.appsueldo.config.FintocProperties;
import com.appsueldo.dto.BankAccountSummaryDto;
import com.appsueldo.dto.BankConnectionResponse;
import com.appsueldo.dto.CreateFintocLinkIntentResponse;
import com.appsueldo.entity.BankConnectionStatus;
import com.appsueldo.entity.BankProvider;
import java.util.List;
import org.junit.jupiter.api.Test;

class FintocSensitiveDtoTest {

    @Test
    void fintocPropertiesDoesNotExposeSecretsInToString() {
        FintocProperties properties = new FintocProperties(
            "sk_test_secret",
            "pk_test_public",
            "https://api.fintoc.com/",
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
            "test",
            90,
            "whsec_secret"
        );

        String text = properties.toString();

        assertThat(text).doesNotContain("sk_test_secret");
        assertThat(text).doesNotContain("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        assertThat(text).doesNotContain("whsec_secret");
        assertThat(text).contains("secretKey=****");
        assertThat(text).contains("publicKey=pk_test_public");
        assertThat(properties.baseUrl()).isEqualTo("https://api.fintoc.com");
    }

    @Test
    void publicAppDtosDoNotExposeFintocSecretsOrRawTokens() {
        CreateFintocLinkIntentResponse linkIntentResponse = new CreateFintocLinkIntentResponse(
            BankProvider.FINTOC,
            "pk_test_public",
            "widget_token_secret",
            "cl",
            "movements"
        );
        BankConnectionResponse bankConnectionResponse = new BankConnectionResponse(
            1L,
            BankProvider.FINTOC,
            "Banco",
            BankConnectionStatus.ACTIVE,
            List.of(new BankAccountSummaryDto(10L, "Cuenta Vista", "sight_account", "CLP", null)),
            3,
            1,
            "COMPLETED"
        );

        assertThat(CreateFintocLinkIntentResponse.class.getRecordComponents())
            .extracting(component -> component.getName())
            .doesNotContain("secretKey", "linkToken", "accessTokenRef", "exchangeToken", "authorization");
        assertThat(BankConnectionResponse.class.getRecordComponents())
            .extracting(component -> component.getName())
            .doesNotContain("secretKey", "linkToken", "accessTokenRef", "exchangeToken", "authorization");
        assertThat(BankAccountSummaryDto.class.getRecordComponents())
            .extracting(component -> component.getName())
            .doesNotContain("number", "holderId", "holderName", "rawPayload");
        assertThat(linkIntentResponse.toString()).doesNotContain("sk_test_secret", "link_token_secret");
        assertThat(bankConnectionResponse.toString()).doesNotContain("link_token_secret", "encryptedLinkToken");
    }

    @Test
    void fintocLinkResponsesDoNotExposeTokensInToString() {
        FintocLinkIntentResponse linkIntent = new FintocLinkIntentResponse(
            "li_123",
            "link_intent",
            "created",
            "movements",
            "cl",
            "individual",
            "widget_token_secret",
            "exchange_token_secret"
        );
        FintocLinkResponse link = new FintocLinkResponse(
            "link_123",
            "link",
            "active",
            "link_token_secret",
            new FintocInstitutionResponse("ins_123", "Banco", "cl"),
            List.of()
        );

        assertThat(linkIntent.toString()).doesNotContain("widget_token_secret");
        assertThat(linkIntent.toString()).doesNotContain("exchange_token_secret");
        assertThat(linkIntent.toString()).contains("widgetToken=****");
        assertThat(linkIntent.toString()).contains("exchangeToken=****");
        assertThat(link.toString()).doesNotContain("link_token_secret");
        assertThat(link.toString()).contains("linkToken=****");
    }

    @Test
    void fintocAccountResponseDoesNotExposeAccountIdentifiersInToString() {
        FintocAccountResponse account = new FintocAccountResponse(
            "acc_123",
            "account",
            "Cuenta corriente",
            "Cuenta corriente",
            "123456789",
            "11111111-1",
            "Persona AppSueldo",
            "checking_account",
            "CLP",
            new FintocBalanceResponse(1000L, 1000L, null),
            null,
            null,
            false,
            "succeeded"
        );

        String text = account.toString();

        assertThat(text).doesNotContain("123456789");
        assertThat(text).doesNotContain("11111111-1");
        assertThat(text).contains("number=****");
        assertThat(text).contains("holderId=****");
    }
}
