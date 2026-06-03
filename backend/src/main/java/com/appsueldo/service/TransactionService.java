package com.appsueldo.service;

import com.appsueldo.dto.TransactionRequest;
import com.appsueldo.dto.TransactionResponse;
import com.appsueldo.entity.Category;
import com.appsueldo.entity.Transaction;
import com.appsueldo.entity.TransactionSource;
import com.appsueldo.entity.User;
import com.appsueldo.exception.ResourceNotFoundException;
import com.appsueldo.repository.CategoryRepository;
import com.appsueldo.repository.TransactionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    public TransactionService(
        TransactionRepository transactionRepository,
        CategoryRepository categoryRepository,
        CurrentUserService currentUserService
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
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
        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findByIdAndUser(request.categoryId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada."));
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setType(request.type());
        transaction.setSource(request.source() == null ? TransactionSource.MANUAL : request.source());
        transaction.setNotes(request.notes());

        return TransactionResponse.from(transactionRepository.save(transaction));
    }
}
