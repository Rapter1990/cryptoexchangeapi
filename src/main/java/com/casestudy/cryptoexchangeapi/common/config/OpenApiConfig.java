package com.casestudy.cryptoexchangeapi.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

/**
 * Configuration class for setting up OpenAPI documentation for the application.
 * This class configures the metadata for the API documentation using OpenAPI 3.0 annotations.
 */
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Sercan Noyan Germiyanoğlu",
                        url = "https://github.com/Rapter1990/cryptoexchangeapi"
                ),
                description = "Case Study - Crypto Exchange Api" +
                        " (Java 25, Spring Boot, Mongodb, JUnit, Docker, Kubernetes, Prometheus, Grafana, Github Actions (CI/CD), Jenkins) ",
                title = "cryptoexchangeapi",
                version = "1.0.0"
        )
)
public class OpenApiConfig {

}
