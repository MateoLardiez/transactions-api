package com.challenge.transactions.exception;

public class SelfReferencingTransactionException extends RuntimeException {

    public SelfReferencingTransactionException(long id) {
        super("Transaction with id " + id + " cannot be its own parent");
    }
}
