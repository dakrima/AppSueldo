package com.appsueldo.service;

import com.appsueldo.dto.ImportBatchResponse;
import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.ImportBatch;
import com.appsueldo.entity.ImportBatchStatus;
import com.appsueldo.entity.Transaction;
import com.appsueldo.entity.TransactionSource;
import com.appsueldo.entity.User;
import com.appsueldo.exception.BadRequestException;
import com.appsueldo.exception.ResourceNotFoundException;
import com.appsueldo.repository.ImportBatchRepository;
import com.appsueldo.repository.TransactionRepository;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TransactionImportService {

    private static final String DEFAULT_FILENAME = "movimientos.csv";

    private final ImportBatchRepository importBatchRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    private final BankAccountService bankAccountService;
    private final TransactionCsvParser parser;

    public TransactionImportService(
        ImportBatchRepository importBatchRepository,
        TransactionRepository transactionRepository,
        CurrentUserService currentUserService,
        BankAccountService bankAccountService,
        TransactionCsvParser parser
    ) {
        this.importBatchRepository = importBatchRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
        this.bankAccountService = bankAccountService;
        this.parser = parser;
    }

    @Transactional
    public ImportBatchResponse importCurrentUserCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Debes subir un archivo CSV con movimientos.");
        }

        User user = currentUserService.currentUser();
        BankAccount bankAccount = bankAccountService.getOrCreateManualAccount(user);
        ImportBatch batch = createBatch(user, bankAccount, originalFilename(file));

        try {
            TransactionCsvParser.ParseResult parseResult = parser.parse(file.getInputStream(), bankAccount);
            Set<String> externalIdsInFile = new HashSet<>();
            int created = 0;
            int skipped = 0;

            for (TransactionCsvParser.ValidRow row : parseResult.validRows()) {
                if (!externalIdsInFile.add(row.externalId()) || transactionRepository.existsByBankAccountAndSourceAndExternalId(
                    bankAccount,
                    TransactionSource.CSV_IMPORT,
                    row.externalId()
                )) {
                    skipped++;
                    continue;
                }

                transactionRepository.save(transaction(user, bankAccount, batch, row));
                created++;
            }

            batch.setTotalRows(parseResult.totalRows());
            batch.setCreatedCount(created);
            batch.setSkippedCount(skipped);
            batch.setInvalidCount(parseResult.invalidRows().size());
            batch.setStatus(ImportBatchStatus.COMPLETED);
            return ImportBatchResponse.from(importBatchRepository.save(batch));
        } catch (IOException exception) {
            batch.setStatus(ImportBatchStatus.FAILED);
            batch.setFailureReason("No pudimos leer el archivo CSV.");
            importBatchRepository.save(batch);
            throw new BadRequestException("No pudimos leer el archivo CSV.");
        } catch (RuntimeException exception) {
            batch.setStatus(ImportBatchStatus.FAILED);
            batch.setFailureReason(exception.getMessage());
            importBatchRepository.save(batch);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public ImportBatchResponse getCurrentUserBatch(Long id) {
        User user = currentUserService.currentUser();
        ImportBatch batch = importBatchRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Importacion no encontrada."));
        return ImportBatchResponse.from(batch);
    }

    private ImportBatch createBatch(User user, BankAccount bankAccount, String originalFilename) {
        ImportBatch batch = new ImportBatch();
        batch.setUser(user);
        batch.setBankAccount(bankAccount);
        batch.setOriginalFilename(originalFilename);
        batch.setImportSource("CSV");
        batch.setStatus(ImportBatchStatus.PROCESSING);
        return importBatchRepository.save(batch);
    }

    private Transaction transaction(
        User user,
        BankAccount bankAccount,
        ImportBatch batch,
        TransactionCsvParser.ValidRow row
    ) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setBankAccount(bankAccount);
        transaction.setImportBatch(batch);
        transaction.setAmount(row.amount());
        transaction.setCurrency(row.currency());
        transaction.setDescription(row.description());
        transaction.setTransactionDate(row.transactionDate());
        transaction.setType(row.type());
        transaction.setSource(TransactionSource.CSV_IMPORT);
        transaction.setExternalId(row.externalId());
        transaction.setNotes("Importado desde CSV.");
        return transaction;
    }

    private String originalFilename(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            return DEFAULT_FILENAME;
        }
        return filename.trim();
    }
}
