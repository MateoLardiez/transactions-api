package com.challenge.transactions.repository;

import com.challenge.transactions.model.Transaction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<Long, Transaction> transactionsById = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> childrenIdsByParentId = new ConcurrentHashMap<>();

    @Override
    public void save(Transaction transaction) {
        transactionsById.put(transaction.id(), transaction);

        if (transaction.parentId() != null) {
            childrenIdsByParentId
                    .computeIfAbsent(transaction.parentId(), parentId -> ConcurrentHashMap.newKeySet())
                    .add(transaction.id());
        }
    }

    @Override
    public Optional<Transaction> findById(long id) {
        return Optional.ofNullable(transactionsById.get(id));
    }

    @Override
    public List<Long> findIdsByType(String type) {
        return transactionsById.values().stream()
                .filter(transaction -> transaction.type().equals(type))
                .map(Transaction::id)
                .toList();
    }

    @Override
    public List<Long> findChildrenIds(long parentId) {
        return List.copyOf(childrenIdsByParentId.getOrDefault(parentId, Set.of()));
    }
}
