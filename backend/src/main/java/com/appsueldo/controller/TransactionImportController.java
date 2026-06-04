package com.appsueldo.controller;

import com.appsueldo.dto.ImportBatchResponse;
import com.appsueldo.service.TransactionImportService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/transactions/imports")
public class TransactionImportController {

    private final TransactionImportService transactionImportService;

    public TransactionImportController(TransactionImportService transactionImportService) {
        this.transactionImportService = transactionImportService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportBatchResponse importCsv(@RequestParam("file") MultipartFile file) {
        return transactionImportService.importCurrentUserCsv(file);
    }

    @GetMapping("/{id}")
    public ImportBatchResponse getBatch(@PathVariable Long id) {
        return transactionImportService.getCurrentUserBatch(id);
    }
}
