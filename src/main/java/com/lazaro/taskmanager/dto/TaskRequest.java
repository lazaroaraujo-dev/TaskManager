package com.lazaro.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record TaskRequest(
        @NotBlank(message = "O título da tarefa é obrigatório")
        String title,

        String description,

        @NotNull(message = "O ID do usuário é obrigatório")
        String userId,

        Set<String> tags,

        // Opcional: tempo de vida em segundos (ex: 86400 para expirar em 24h)
        Long ttl
) {}