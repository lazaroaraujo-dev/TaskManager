package com.lazaro.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record TaskRequest(
        @NotBlank(message = "O título da tarefa é obrigatório")
        String title,

        String description,

        @NotBlank(message = "O ID do usuário é obrigatório")
        String userId,

        // Padrão de mercado: opcional. Fica sem anotações de validação.
        String projectId,

        Set<String> tags,

        Long ttl
) {}