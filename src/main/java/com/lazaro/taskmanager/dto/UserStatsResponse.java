package com.lazaro.taskmanager.dto;

public record UserStatsResponse(
        String userId,
        int tarefasConcluidas
) {}