package com.challenge.transactions.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 5000, "type": "cars" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        { "status": "ok" }
                        """));
    }

    @Test
    void putTransactionStoresItsParentId() throws Exception {
        mockMvc.perform(put("/transactions/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "amount": 5000, "type": "cars" }
                        """));

        mockMvc.perform(put("/transactions/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 10000, "type": "shopping", "parent_id": 10 }
                                """))
                .andExpect(status().isCreated());

        assertThat(repository.findById(11L))
                .contains(new Transaction(11L, 10000.0, "shopping", 10L));
    }

    @Test
    void putTransactionWithoutAmountReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "cars" }
                                """))
                .andExpect(status().isBadRequest());

        assertThat(repository.findById(10L)).isEmpty();
    }

    @Test
    void putTransactionWithBlankTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 5000, "type": "  " }
                                """))
                .andExpect(status().isBadRequest());

        assertThat(repository.findById(10L)).isEmpty();
    }
}
