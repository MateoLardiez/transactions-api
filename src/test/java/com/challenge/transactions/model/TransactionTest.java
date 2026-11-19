package com.challenge.transactions.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TransactionTest {

    @Test
    void cannotBeCreatedWithNullType() {
        assertThatThrownBy(() -> new Transaction(10L, 5000.0, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("type must not be null");
    }
}
