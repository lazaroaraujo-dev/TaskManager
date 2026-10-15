package com.lazaro.taskmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Manager API - Valkey NoSQL")
                        .version("1.0.0")
                        .description("API REST desenvolvida para gerenciamento de tarefas utilizando Valkey como banco NoSQL de alta performance (chave-valor, hashes e sets).")
                        .contact(new Contact()
                                .name("Lázaro Araújo")));
    }
}