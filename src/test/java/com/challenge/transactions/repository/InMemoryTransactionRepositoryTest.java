package com.challenge.transactions.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.challenge.transactions.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryTransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
    }

    @Test
    void savedTransactionCanBeFoundById() {
        var transaction = new Transaction(10L, 5000.0, "cars", null);

        boolean saved = repository.saveIfAbsent(transaction);

        assertThat(saved).isTrue();
        assertThat(repository.findById(10L)).contains(transaction);
    }

    @Test
    void saveIfAbsentDoesNotOverwriteExistingTransaction() {
        var original = new Transaction(10L, 5000.0, "cars", null);
        repository.saveIfAbsent(original);

        boolean saved = repository.saveIfAbsent(new Transaction(10L, 999.0, "food", null));

        assertThat(saved).isFalse();
        assertThat(repository.findById(10L)).contains(original);
    }

    @Test
    void rejectedDuplicateIsNotAddedToChildrenIndex() {
        repository.saveIfAbsent(new Transaction(10L, 5000.0, "cars", null));
        repository.saveIfAbsent(new Transaction(20L, 3000.0, "cars", null));
        repository.saveIfAbsent(new Transaction(11L, 10000.0, "shopping", 10L));

        repository.saveIfAbsent(new Transaction(11L, 10000.0, "shopping", 20L));

        assertThat(repository.findChildrenIds(20L)).isEmpty();
    }

    @Test
    void findIdsByTypeReturnsOnlyIdsOfThatType() {
        repository.saveIfAbsent(new Transaction(10L, 5000.0, "cars", null));
        repository.saveIfAbsent(new Transaction(11L, 10000.0, "shopping", 10L));
        repository.saveIfAbsent(new Transaction(12L, 5000.0, "shopping", 11L));

        assertThat(repository.findIdsByType("shopping")).containsExactlyInAnyOrder(11L, 12L);
    }

    @Test
    void findIdsByTypeReturnsEmptyListWhenNoTransactionHasThatType() {
        repository.saveIfAbsent(new Transaction(10L, 5000.0, "cars", null));

        assertThat(repository.findIdsByType("food")).isEmpty();
    }

    @Test
    void findChildrenIdsReturnsOnlyDirectChildren() {
        repository.saveIfAbsent(new Transaction(10L, 5000.0, "cars", null));
        repository.saveIfAbsent(new Transaction(11L, 10000.0, "shopping", 10L));
        repository.saveIfAbsent(new Transaction(12L, 5000.0, "shopping", 11L));
        repository.saveIfAbsent(new Transaction(13L, 2000.0, "food", 10L));

        assertThat(repository.findChildrenIds(10L)).containsExactlyInAnyOrder(11L, 13L);
    }

    @Test
    void findChildrenIdsReturnsEmptyListWhenTransactionHasNoChildren() {
        repository.saveIfAbsent(new Transaction(10L, 5000.0, "cars", null));

        assertThat(repository.findChildrenIds(10L)).isEmpty();
    }
}
