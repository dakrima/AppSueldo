package com.appsueldo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.appsueldo.dto.ImportBatchResponse;
import com.appsueldo.entity.ImportBatchStatus;
import com.appsueldo.exception.ApiExceptionHandler;
import com.appsueldo.service.TransactionImportService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

class TransactionImportControllerTest {

    private FakeTransactionImportService transactionImportService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        transactionImportService = new FakeTransactionImportService(response());
        TransactionImportController controller = new TransactionImportController(transactionImportService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void uploadCsvReturnsBatchSummary() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "movimientos.csv",
            "text/csv",
            "fecha,monto,descripcion\n2026-06-01,-12500,Cafe\n".getBytes()
        );

        mockMvc.perform(multipart("/api/transactions/imports")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(55))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.createdCount").value(3))
            .andExpect(jsonPath("$.skippedCount").value(1))
            .andExpect(jsonPath("$.invalidCount").value(2));

        org.assertj.core.api.Assertions.assertThat(transactionImportService.uploadedFilename)
            .isEqualTo("movimientos.csv");
    }

    @Test
    void getBatchReturnsBatchSummary() throws Exception {
        mockMvc.perform(get("/api/transactions/imports/55"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.originalFilename").value("movimientos.csv"))
            .andExpect(jsonPath("$.bankAccountName").value("Cuenta manual"));

        org.assertj.core.api.Assertions.assertThat(transactionImportService.requestedBatchId)
            .isEqualTo(55L);
    }

    private ImportBatchResponse response() {
        Instant now = Instant.parse("2026-06-04T12:00:00Z");
        return new ImportBatchResponse(
            55L,
            ImportBatchStatus.COMPLETED,
            "movimientos.csv",
            "CSV",
            10L,
            "Cuenta manual",
            6,
            3,
            1,
            2,
            null,
            now,
            now
        );
    }

    private static class FakeTransactionImportService extends TransactionImportService {
        private final ImportBatchResponse response;
        private String uploadedFilename;
        private Long requestedBatchId;

        FakeTransactionImportService(ImportBatchResponse response) {
            super(null, null, null, null, null);
            this.response = response;
        }

        @Override
        public ImportBatchResponse importCurrentUserCsv(MultipartFile file) {
            uploadedFilename = file.getOriginalFilename();
            return response;
        }

        @Override
        public ImportBatchResponse getCurrentUserBatch(Long id) {
            requestedBatchId = id;
            return response;
        }
    }
}
