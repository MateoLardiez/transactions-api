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

        repository.save(transaction);

        assertThat(repository.findById(10L)).contains(transaction);
    }

    @Test
    void findIdsByTypeReturnsOnlyIdsOfThatType() {
        repository.save(new Transaction(10L, 5000.0, "cars", null));
        repository.save(new Transaction(11L, 10000.0, "shopping", 10L));
        repository.save(new Transaction(12L, 5000.0, "shopping", 11L));

        assertThat(repository.findIdsByType("shopping")).containsExactlyInAnyOrder(11L, 12L);
    }

    @Test
    void findIdsByTypeReturnsEmptyListWhenNoTransactionHasThatType() {
        repository.save(new Transaction(10L, 5000.0, "cars", null));

        assertThat(repository.findIdsByType("food")).isEmpty();
    }
}
