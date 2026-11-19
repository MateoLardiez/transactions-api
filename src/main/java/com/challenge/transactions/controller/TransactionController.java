package com.challenge.transactions.controller;

import com.challenge.transactions.controller.dto.StatusResponse;
import com.challenge.transactions.controller.dto.TransactionRequest;
import com.challenge.transactions.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public StatusResponse create(@PathVariable long id, @Valid @RequestBody TransactionRequest request) {
        service.create(request.toTransaction(id));
        return new StatusResponse("ok");
    }

    @GetMapping("/types/{type}")
    public List<Long> findIdsByType(@PathVariable String type) {
        return service.findIdsByType(type);
    }
}
