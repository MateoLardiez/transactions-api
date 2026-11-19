package com.challenge.transactions.model;

public record Transaction(long id, double amount, String type, Long parentId) {}
