package com.challenge.transactions.service;

import com.challenge.transactions.exception.DuplicateTransactionException;
import com.challenge.transactions.exception.ParentTransactionNotFoundException;
import com.challenge.transactions.exception.SelfReferencingTransactionException;
import com.challenge.transactions.model.Transaction;
import com.challenge.transactions.repository.TransactionRepository;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
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

    public List<Long> findIdsByType(String type) {
        return repository.findIdsByType(type);
    }

    public double sum(long id) {
        double total = 0;
        Deque<Long> pending = new ArrayDeque<>();
        pending.add(id);

        while (!pending.isEmpty()) {
            long currentId = pending.poll();
            total += repository.findById(currentId).orElseThrow().amount();
            pending.addAll(repository.findChildrenIds(currentId));
        }
        return total;
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
