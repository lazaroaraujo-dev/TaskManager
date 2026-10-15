package com.lazaro.taskmanager.dto;

import com.lazaro.taskmanager.model.User;

public record UserResponse(
        String id,
        String name,
        String email
) {
    public UserResponse(User user) {
        this(user.getId(), user.getName(), user.getEmail());
    }
}