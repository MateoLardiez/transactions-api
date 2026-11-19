package com.challenge.transactions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.challenge.transactions.exception.DuplicateTransactionException;
import com.challenge.transactions.model.Transaction;
import com.challenge.transactions.repository.InMemoryTransactionRepository;
import com.challenge.transactions.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionServiceTest {

    private TransactionRepository repository;
    private TransactionService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
        service = new TransactionService(repository);
    }

    @Test
    void createdTransactionIsStored() {
        var transaction = new Transaction(10L, 5000.0, "cars", null);

        service.create(transaction);

        assertThat(repository.findById(10L)).contains(transaction);
    }

    @Test
    void creatingTransactionWithExistingIdIsRejected() {
        var original = new Transaction(10L, 5000.0, "cars", null);
        service.create(original);

        assertThatThrownBy(() -> service.create(new Transaction(10L, 999.0, "food", null)))
                .isInstanceOf(DuplicateTransactionException.class)
                .hasMessage("Transaction with id 10 already exists");
        assertThat(repository.findById(10L)).contains(original);
    }
}
