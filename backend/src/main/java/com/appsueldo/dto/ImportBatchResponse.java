package com.appsueldo.dto;

import com.appsueldo.entity.ImportBatch;
import com.appsueldo.entity.ImportBatchStatus;
import java.time.Instant;

public record ImportBatchResponse(
    Long id,
    ImportBatchStatus status,
    String originalFilename,
    String importSource,
    Long bankAccountId,
    String bankAccountName,
    int totalRows,
    int createdCount,
    int skippedCount,
    int invalidCount,
    String failureReason,
    Instant createdAt,
    Instant updatedAt
) {
    public static ImportBatchResponse from(ImportBatch batch) {
        return new ImportBatchResponse(
            batch.getId(),
            batch.getStatus(),
            batch.getOriginalFilename(),
            batch.getImportSource(),
            batch.getBankAccount().getId(),
            batch.getBankAccount().getName(),
            batch.getTotalRows(),
            batch.getCreatedCount(),
            batch.getSkippedCount(),
            batch.getInvalidCount(),
            batch.getFailureReason(),
            batch.getCreatedAt(),
            batch.getUpdatedAt()
        );
    }
}
