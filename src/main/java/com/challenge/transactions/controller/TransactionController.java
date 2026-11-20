package com.challenge.transactions.controller;

import com.challenge.transactions.controller.dto.StatusResponse;
import com.challenge.transactions.controller.dto.SumResponse;
import com.challenge.transactions.controller.dto.TransactionRequest;
import com.challenge.transactions.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Transactions")
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @Operation(summary = "Create a transaction, optionally linked to a parent")
    @ApiResponse(responseCode = "201", description = "Transaction created")
    @ApiResponse(responseCode = "400", description = "Invalid body, non-existent parent or self-referencing parent",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "A transaction with that id already exists",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public StatusResponse create(@PathVariable long id, @Valid @RequestBody TransactionRequest request) {
        service.create(request.toTransaction(id));
        return new StatusResponse("ok");
    }

    @Operation(summary = "List the ids of all transactions of a type")
    @ApiResponse(responseCode = "200", description = "Ids of that type, empty if there are none")
    @GetMapping("/types/{type}")
    public List<Long> findIdsByType(@PathVariable String type) {
        return service.findIdsByType(type);
    }

    @Operation(summary = "Sum the amount of a transaction and all its descendants")
    @ApiResponse(responseCode = "200", description = "Transitive sum calculated")
    @ApiResponse(responseCode = "404", description = "The transaction does not exist",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/sum/{id}")
    public SumResponse sum(@PathVariable long id) {
        return new SumResponse(service.sum(id));
    }
}
