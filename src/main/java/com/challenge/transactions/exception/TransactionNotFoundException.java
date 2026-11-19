package com.challenge.transactions.exception;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(long id) {
        super("Transaction with id " + id + " does not exist");
    }
}
