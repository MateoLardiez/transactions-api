package com.challenge.transactions.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsDescribeAllEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/transactions/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/transactions/types/{type}'].get").exists())
                .andExpect(jsonPath("$.paths['/transactions/sum/{id}'].get").exists());
    }

    @Test
    void swaggerUiIsAvailable() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }
}
