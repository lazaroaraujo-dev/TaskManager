package com.lazaro.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectRequest(
        @NotBlank(message = "O nome do projeto é obrigatório")
        String name,

        String description,

        @NotNull(message = "O ID do utilizador é obrigatório")
        String userId
) {}