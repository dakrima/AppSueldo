package com.appsueldo.service;

import com.appsueldo.dto.AssignTransactionCategoryRequest;
import com.appsueldo.dto.TransactionRequest;
import com.appsueldo.dto.TransactionResponse;
import com.appsueldo.entity.BankAccount;
import com.appsueldo.entity.Category;
import com.appsueldo.entity.ClassificationMethod;
import com.appsueldo.entity.Transaction;
import com.appsueldo.entity.TransactionClassification;
import com.appsueldo.entity.TransactionSource;
import com.appsueldo.entity.User;
import com.appsueldo.exception.BadRequestException;
import com.appsueldo.exception.ResourceNotFoundException;
import com.appsueldo.repository.BankAccountRepository;
import com.appsueldo.repository.CategoryRepository;
import com.appsueldo.repository.TransactionClassificationRepository;
import com.appsueldo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionClassificationRepository transactionClassificationRepository;
    private final CurrentUserService currentUserService;
    private final BankAccountService bankAccountService;

    public TransactionService(
        TransactionRepository transactionRepository,
        CategoryRepository categoryRepository,
        BankAccountRepository bankAccountRepository,
        TransactionClassificationRepository transactionClassificationRepository,
        CurrentUserService currentUserService,
        BankAccountService bankAccountService
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.transactionClassificationRepository = transactionClassificationRepository;
        this.currentUserService = currentUserService;
        this.bankAccountService = bankAccountService;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listCurrentUserTransactions() {
        User user = currentUserService.currentUser();
        return transactionRepository.findByUserOrderByTransactionDateDescIdDesc(user).stream()
            .map(TransactionResponse::from)
            .toList();
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        User user = currentUserService.currentUser();
        if (request.source() == TransactionSource.FINTOC) {
            throw new BadRequestException("No se pueden crear transacciones Fintoc manualmente.");
        }

        BankAccount bankAccount = request.bankAccountId() == null
            ? bankAccountService.getOrCreateManualAccount(user)
            : bankAccountRepository.findByIdAndUser(request.bankAccountId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta bancaria no encontrada."));

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findByIdAndUser(request.categoryId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada."));
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setBankAccount(bankAccount);
        transaction.setCategory(category);
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency());
        transaction.setDescription(request.description());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setType(request.type());
        transaction.setSource(TransactionSource.MANUAL);
        transaction.setNotes(request.notes());

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse assignCategory(Long transactionId, AssignTransactionCategoryRequest request) {
        User user = currentUserService.currentUser();
        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
            .orElseThrow(() -> new ResourceNotFoundException("Transaccion no encontrada."));

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findByIdAndUser(request.categoryId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada."));
        }

        transaction.setCategory(category);
        upsertManualClassification(user, transaction, category);
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    private void upsertManualClassification(User user, Transaction transaction, Category category) {
        TransactionClassification classification = transactionClassificationRepository
            .findByTransactionAndUser(transaction, user)
            .orElseGet(TransactionClassification::new);
        classification.setUser(user);
        classification.setTransaction(transaction);
        classification.setCategory(category);
        classification.setMethod(ClassificationMethod.MANUAL);
        classification.setConfidence(BigDecimal.ONE);
        classification.setReason(
            category == null ? "Categoria quitada manualmente." : "Categoria asignada manualmente."
        );
        transactionClassificationRepository.save(classification);
    }
}
