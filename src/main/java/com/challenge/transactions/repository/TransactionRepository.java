package com.challenge.transactions.repository;

import com.challenge.transactions.model.Transaction;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    void save(Transaction transaction);

    Optional<Transaction> findById(long id);

    List<Long> findIdsByType(String type);
}
