package com.lazaro.taskmanager.dto;

import com.lazaro.taskmanager.model.StatusTarefa;
import com.lazaro.taskmanager.model.Task;
import java.util.Set;

public record TaskResponse(
        String id,
        String title,
        String description,
        StatusTarefa status,
        String userId,
        String projectId,
        Set<String> tags,
        Long ttl
) {
    public TaskResponse(Task task) {
        this(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getUserId(),
                task.getProjectId(),
                task.getTags(),
                task.getTtl()
        );
    }
}