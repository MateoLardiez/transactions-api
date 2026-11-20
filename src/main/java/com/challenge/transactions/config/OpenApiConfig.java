package com.challenge.transactions.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI transactionsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Transactions API")
                .description("Stores transactions in memory, lists them by type and calculates "
                        + "the transitive sum of a transaction and all its descendants.")
                .version("1.0.0"));
    }
}
