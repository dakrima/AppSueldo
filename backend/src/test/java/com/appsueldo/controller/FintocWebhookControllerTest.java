package com.appsueldo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.appsueldo.exception.ApiExceptionHandler;
import com.appsueldo.exception.BadRequestException;
import com.appsueldo.service.FintocWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FintocWebhookControllerTest {

    private FakeFintocWebhookService fintocWebhookService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        fintocWebhookService = new FakeFintocWebhookService();
        FintocWebhookController controller = new FintocWebhookController(fintocWebhookService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void webhookReturnsAcceptedResponse() throws Exception {
        String payload = "{\"id\":\"evt_123\",\"type\":\"account.refresh_intent.succeeded\"}";

        mockMvc.perform(post("/api/webhooks/fintoc")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Fintoc-Signature", "t=1,v1=signature")
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("accepted"))
            .andExpect(jsonPath("$.secretKey").doesNotExist())
            .andExpect(jsonPath("$.linkToken").doesNotExist())
            .andExpect(jsonPath("$.accessTokenRef").doesNotExist());

        assertThat(fintocWebhookService.rawBody).isEqualTo(payload);
        assertThat(fintocWebhookService.signature).isEqualTo("t=1,v1=signature");
    }

    @Test
    void webhookInvalidSignatureReturnsBadRequest() throws Exception {
        fintocWebhookService.fail = true;

        mockMvc.perform(post("/api/webhooks/fintoc")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Fintoc-Signature", "invalid")
                .content("{\"id\":\"evt_123\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Firma Fintoc invalida."));
    }

    private static class FakeFintocWebhookService extends FintocWebhookService {
        private boolean fail;
        private String rawBody;
        private String signature;

        FakeFintocWebhookService() {
            super(null, null, null, null, null, null);
        }

        @Override
        public void handle(String rawBody, String signatureHeader) {
            this.rawBody = rawBody;
            this.signature = signatureHeader;
            if (fail) {
                throw new BadRequestException("Firma Fintoc invalida.");
            }
        }
    }
}
