package com.challenge.transactions.repository;

import com.challenge.transactions.model.Transaction;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    /**
     * Saves the transaction only if no transaction with the same id exists.
     * The check and the save happen atomically.
     *
     * @return true if it was saved, false if the id already existed
     */
    boolean saveIfAbsent(Transaction transaction);

    Optional<Transaction> findById(long id);

    List<Long> findIdsByType(String type);

    List<Long> findChildrenIds(long parentId);
}
