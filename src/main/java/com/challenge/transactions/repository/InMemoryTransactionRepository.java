package com.challenge.transactions.repository;

import com.challenge.transactions.model.Transaction;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<Long, Transaction> transactionsById = new ConcurrentHashMap<>();

    @Override
    public void save(Transaction transaction) {
        transactionsById.put(transaction.id(), transaction);
    }

    @Override
    public Optional<Transaction> findById(long id) {
        return Optional.ofNullable(transactionsById.get(id));
    }
}
