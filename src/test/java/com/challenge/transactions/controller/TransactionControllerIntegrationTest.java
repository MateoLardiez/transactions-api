package com.challenge.transactions.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.transactions.model.Transaction;
import com.challenge.transactions.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository repository;

    @Test
    void putTransactionReturnsCreated() throws Exception {
        putTransaction(10, """
                { "amount": 5000, "type": "cars" }
                """)
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        { "status": "ok" }
                        """));
    }

    @Test
    void putTransactionStoresItsParentId() throws Exception {
        createTransaction(10, """
                { "amount": 5000, "type": "cars" }
                """);

        createTransaction(11, """
                { "amount": 10000, "type": "shopping", "parent_id": 10 }
                """);

        assertThat(repository.findById(11L))
                .contains(new Transaction(11L, 10000.0, "shopping", 10L));
    }

    @Test
    void putTransactionWithoutAmountReturnsBadRequest() throws Exception {
        putTransaction(10, """
                { "type": "cars" }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.amount").value("must not be null"));

        assertThat(repository.findById(10L)).isEmpty();
    }

    @Test
    void putTransactionWithBlankTypeReturnsBadRequest() throws Exception {
        putTransaction(10, """
                { "amount": 5000, "type": "  " }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.type").value("must not be blank"));

        assertThat(repository.findById(10L)).isEmpty();
    }

    @Test
    void putTransactionWithNonNumericAmountReturnsBadRequestWithField() throws Exception {
        putTransaction(10, """
                { "amount": "aa", "type": "cars" }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.amount").value("must be a number"));

        assertThat(repository.findById(10L)).isEmpty();
    }

    @Test
    void putTransactionWithAmountAsStringReturnsBadRequest() throws Exception {
        putTransaction(10, """
                { "amount": "2", "type": "cars" }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").value("must be a number"));

        assertThat(repository.findById(10L)).isEmpty();
    }

    @Test
    void putTransactionWithInvalidTypeValueReturnsBadRequestWithField() throws Exception {
        putTransaction(10, """
                { "amount": 5000, "type": { "name": "cars" } }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.type").value("has an invalid value"));
    }

    @Test
    void validationMessagesDoNotDependOnClientLanguage() throws Exception {
        mockMvc.perform(put("/transactions/10")
                        .header("Accept-Language", "es")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").value("must not be null"))
                .andExpect(jsonPath("$.errors.type").value("must not be blank"));
    }

    @Test
    void putTransactionWithExistingIdReturnsConflict() throws Exception {
        createTransaction(10, """
                { "amount": 5000, "type": "cars" }
                """);

        putTransaction(10, """
                { "amount": 999, "type": "food" }
                """)
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Transaction with id 10 already exists"));

        assertThat(repository.findById(10L))
                .contains(new Transaction(10L, 5000.0, "cars", null));
    }

    @Test
    void putTransactionWithNonExistentParentReturnsBadRequest() throws Exception {
        putTransaction(11, """
                { "amount": 10000, "type": "shopping", "parent_id": 10 }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Parent transaction with id 10 does not exist"));

        assertThat(repository.findById(11L)).isEmpty();
    }

    @Test
    void putTransactionThatIsItsOwnParentReturnsBadRequest() throws Exception {
        putTransaction(10, """
                { "amount": 5000, "type": "cars", "parent_id": 10 }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Transaction with id 10 cannot be its own parent"));

        assertThat(repository.findById(10L)).isEmpty();
    }

    @Test
    void getByTypeReturnsIdsOfThatType() throws Exception {
        createTransaction(10, """
                { "amount": 5000, "type": "cars" }
                """);
        createTransaction(11, """
                { "amount": 10000, "type": "shopping", "parent_id": 10 }
                """);
        createTransaction(12, """
                { "amount": 5000, "type": "shopping", "parent_id": 11 }
                """);

        mockMvc.perform(get("/transactions/types/shopping"))
                .andExpect(status().isOk())
                .andExpect(content().json("[11, 12]"));
    }

    @Test
    void getByTypeReturnsEmptyListWhenTypeDoesNotExist() throws Exception {
        createTransaction(10, """
                { "amount": 5000, "type": "cars" }
                """);

        mockMvc.perform(get("/transactions/types/food"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getSumReturnsSumOfTransactionAndAllItsDescendants() throws Exception {
        createTransaction(10, """
                { "amount": 1000, "type": "cars" }
                """);
        createTransaction(11, """
                { "amount": 200, "type": "cars", "parent_id": 10 }
                """);
        createTransaction(12, """
                { "amount": 30, "type": "cars", "parent_id": 10 }
                """);
        createTransaction(13, """
                { "amount": 4, "type": "cars", "parent_id": 11 }
                """);

        mockMvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        { "sum": 1234 }
                        """));
    }

    @Test
    void getSumOfNonExistentTransactionReturnsNotFound() throws Exception {
        mockMvc.perform(get("/transactions/sum/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Transaction with id 99 does not exist"))
                .andExpect(jsonPath("$.instance").value("/transactions/sum/99"));
    }

    @Test
    void statementExampleWorksEndToEnd() throws Exception {
        createTransaction(10, """
                { "amount": 5000, "type": "cars" }
                """);
        createTransaction(11, """
                { "amount": 10000, "type": "shopping", "parent_id": 10 }
                """);
        createTransaction(12, """
                { "amount": 5000, "type": "shopping", "parent_id": 11 }
                """);

        mockMvc.perform(get("/transactions/types/cars"))
                .andExpect(status().isOk())
                .andExpect(content().json("[10]"));
        mockMvc.perform(get("/transactions/sum/10"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        { "sum": 20000 }
                        """));
        mockMvc.perform(get("/transactions/sum/11"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        { "sum": 15000 }
                        """));
    }

    private ResultActions putTransaction(long id, String body) throws Exception {
        return mockMvc.perform(put("/transactions/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void createTransaction(long id, String body) throws Exception {
        putTransaction(id, body).andExpect(status().isCreated());
    }
}
