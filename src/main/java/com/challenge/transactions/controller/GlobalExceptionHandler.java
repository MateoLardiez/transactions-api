package com.challenge.transactions.controller;

import com.challenge.transactions.exception.DuplicateTransactionException;
import com.challenge.transactions.exception.ParentTransactionNotFoundException;
import com.challenge.transactions.exception.SelfReferencingTransactionException;
import com.challenge.transactions.exception.TransactionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleTransactionNotFound() {
    }

    @ExceptionHandler(DuplicateTransactionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handleDuplicateTransaction() {
    }

    @ExceptionHandler({ParentTransactionNotFoundException.class, SelfReferencingTransactionException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleInvalidParent() {
    }
}
