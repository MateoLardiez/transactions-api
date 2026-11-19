package com.challenge.transactions.exception;

public class DuplicateTransactionException extends RuntimeException {

    public DuplicateTransactionException(long id) {
        super("Transaction with id " + id + " already exists");
    }
}
