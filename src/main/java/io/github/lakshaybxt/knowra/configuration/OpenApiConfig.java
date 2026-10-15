package io.github.lakshaybxt.knowra.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {

    String securitySchemeName = "bearerAuth";

    return new OpenAPI()
        .info(
            new Info()
                .title("Knowra — AI Document Intelligence & RAG backend")
                .description("REST API for Knowra: Multi-format document ingestion, vector embeddings with PostgreSQL pgvector, and hybrid conversational Q&A with OpenAI.")

                .version("1.0.0")
                .contact(new Contact()
                    .name("lakshaybxt")
                    .email("lakshaybisht.dev@gmail.com")
                    .url("https://lakshaybxt.netlify.app")
                )

        ).components(new Components()
            .addSecuritySchemes(
                securitySchemeName,
                new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            ))

        .addSecurityItem(
            new SecurityRequirement()
                .addList(securitySchemeName)
        );


  }

}
