package com.challenge.transactions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.challenge.transactions.exception.DuplicateTransactionException;
import com.challenge.transactions.exception.ParentTransactionNotFoundException;
import com.challenge.transactions.exception.SelfReferencingTransactionException;
import com.challenge.transactions.exception.TransactionNotFoundException;
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

    @Test
    void transactionWithExistingParentIsStored() {
        service.create(new Transaction(10L, 5000.0, "cars", null));
        var child = new Transaction(11L, 10000.0, "shopping", 10L);

        service.create(child);

        assertThat(repository.findById(11L)).contains(child);
    }

    @Test
    void creatingTransactionWithNonExistentParentIsRejected() {
        var orphan = new Transaction(11L, 10000.0, "shopping", 10L);

        assertThatThrownBy(() -> service.create(orphan))
                .isInstanceOf(ParentTransactionNotFoundException.class)
                .hasMessage("Parent transaction with id 10 does not exist");
        assertThat(repository.findById(11L)).isEmpty();
    }

    @Test
    void creatingTransactionThatIsItsOwnParentIsRejected() {
        var selfReferencing = new Transaction(10L, 5000.0, "cars", 10L);

        assertThatThrownBy(() -> service.create(selfReferencing))
                .isInstanceOf(SelfReferencingTransactionException.class)
                .hasMessage("Transaction with id 10 cannot be its own parent");
        assertThat(repository.findById(10L)).isEmpty();
    }

    @Test
    void findIdsByTypeReturnsIdsOfThatType() {
        service.create(new Transaction(10L, 5000.0, "cars", null));
        service.create(new Transaction(11L, 10000.0, "shopping", 10L));
        service.create(new Transaction(12L, 5000.0, "shopping", 11L));

        assertThat(service.findIdsByType("shopping")).containsExactlyInAnyOrder(11L, 12L);
    }

    @Test
    void findIdsByTypeReturnsEmptyListWhenTypeDoesNotExist() {
        service.create(new Transaction(10L, 5000.0, "cars", null));

        assertThat(service.findIdsByType("food")).isEmpty();
    }

    @Test
    void sumOfTransactionWithoutChildrenIsItsOwnAmount() {
        service.create(new Transaction(10L, 5000.0, "cars", null));

        assertThat(service.sum(10L)).isEqualTo(5000.0);
    }

    @Test
    void sumIncludesAllDescendantsButNotAncestors() {
        service.create(new Transaction(10L, 5000.0, "cars", null));
        service.create(new Transaction(11L, 10000.0, "shopping", 10L));
        service.create(new Transaction(12L, 5000.0, "shopping", 11L));

        assertThat(service.sum(10L)).isEqualTo(20000.0);
        assertThat(service.sum(11L)).isEqualTo(15000.0);
    }

    @Test
    void sumTraversesEveryBranch() {
        service.create(new Transaction(10L, 1000.0, "cars", null));
        service.create(new Transaction(11L, 200.0, "cars", 10L));
        service.create(new Transaction(12L, 30.0, "cars", 10L));
        service.create(new Transaction(13L, 4.0, "cars", 11L));

        assertThat(service.sum(10L)).isEqualTo(1234.0);
    }

    @Test
    void sumOfNonExistentTransactionIsRejected() {
        assertThatThrownBy(() -> service.sum(99L))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transaction with id 99 does not exist");
    }
}
