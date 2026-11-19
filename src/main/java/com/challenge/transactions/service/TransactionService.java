package com.challenge.transactions.service;

import com.challenge.transactions.exception.DuplicateTransactionException;
import com.challenge.transactions.exception.ParentTransactionNotFoundException;
import com.challenge.transactions.exception.SelfReferencingTransactionException;
import com.challenge.transactions.model.Transaction;
import com.challenge.transactions.repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public void create(Transaction transaction) {
        if (transaction.parentId() != null) {
            validateParent(transaction.id(), transaction.parentId());
        }

        if (!repository.saveIfAbsent(transaction)) {
            throw new DuplicateTransactionException(transaction.id());
        }
    }

    private void validateParent(long id, long parentId) {
        if (parentId == id) {
            throw new SelfReferencingTransactionException(id);
        }
        if (repository.findById(parentId).isEmpty()) {
            throw new ParentTransactionNotFoundException(parentId);
        }
    }
}
