package com.challenge.transactions.controller.dto;

import com.challenge.transactions.model.Transaction;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransactionRequest(
        @Schema(description = "Transaction amount", example = "5000")
        @NotNull(message = "must not be null") Double amount,
        @Schema(description = "Transaction type", example = "cars")
        @NotBlank(message = "must not be blank") String type,
        @Schema(description = "Id of the parent transaction, optional", example = "10")
        @JsonProperty("parent_id") Long parentId) {

    public Transaction toTransaction(long id) {
        return new Transaction(id, amount, type, parentId);
    }
}
