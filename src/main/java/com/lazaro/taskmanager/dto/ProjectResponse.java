package com.lazaro.taskmanager.dto;

import com.lazaro.taskmanager.model.Project;

public record ProjectResponse(
        String id,
        String name,
        String description,
        String userId
) {
    public ProjectResponse(Project project) {
        this(project.getId(), project.getName(), project.getDescription(), project.getUserId());
    }
}