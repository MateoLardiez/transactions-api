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
    void apiDocsIncludeGeneralInformation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.info.title").value("Transactions API"))
                .andExpect(jsonPath("$.info.version").value("1.0.0"))
                .andExpect(jsonPath("$.info.description").isNotEmpty());
    }

    @Test
    void eachEndpointDocumentsOnlyItsOwnResponses() throws Exception {
        String put = "$.paths['/transactions/{id}'].put.responses";
        String byType = "$.paths['/transactions/types/{type}'].get.responses";
        String sum = "$.paths['/transactions/sum/{id}'].get.responses";

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath(put + "['201']").exists())
                .andExpect(jsonPath(put + "['400']").exists())
                .andExpect(jsonPath(put + "['409']").exists())
                .andExpect(jsonPath(put + "['404']").doesNotExist())
                .andExpect(jsonPath(byType + "['200']").exists())
                .andExpect(jsonPath(byType + "['404']").doesNotExist())
                .andExpect(jsonPath(byType + "['409']").doesNotExist())
                .andExpect(jsonPath(sum + "['200']").exists())
                .andExpect(jsonPath(sum + "['404']").exists())
                .andExpect(jsonPath(sum + "['409']").doesNotExist());
    }

    @Test
    void errorResponsesAreDocumentedAsProblemDetail() throws Exception {
        String problemSchema = ".content['application/problem+json'].schema['$ref']";
        String problemDetail = "#/components/schemas/ProblemDetail";

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/transactions/{id}'].put.responses['400']" + problemSchema)
                        .value(problemDetail))
                .andExpect(jsonPath("$.paths['/transactions/{id}'].put.responses['409']" + problemSchema)
                        .value(problemDetail))
                .andExpect(jsonPath("$.paths['/transactions/sum/{id}'].get.responses['404']" + problemSchema)
                        .value(problemDetail));
    }

    @Test
    void endpointsHaveSummaries() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/transactions/{id}'].put.summary").isNotEmpty())
                .andExpect(jsonPath("$.paths['/transactions/types/{type}'].get.summary").isNotEmpty())
                .andExpect(jsonPath("$.paths['/transactions/sum/{id}'].get.summary").isNotEmpty());
    }

    @Test
    void swaggerUiIsAvailable() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }
}
