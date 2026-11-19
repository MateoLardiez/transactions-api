package com.challenge.transactions.service;

import com.challenge.transactions.exception.DuplicateTransactionException;
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
        if (!repository.saveIfAbsent(transaction)) {
            throw new DuplicateTransactionException(transaction.id());
        }
    }
}
