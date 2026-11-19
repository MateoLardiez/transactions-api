package com.challenge.transactions.controller.dto;

import com.challenge.transactions.model.Transaction;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransactionRequest(
        @NotNull Double amount,
        @NotBlank String type,
        @JsonProperty("parent_id") Long parentId) {

    public Transaction toTransaction(long id) {
        return new Transaction(id, amount, type, parentId);
    }
}
