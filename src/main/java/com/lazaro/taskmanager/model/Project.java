package com.lazaro.taskmanager.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private String id;
    private String name;
    private String description;
    private String userId; // Pertence a um usuário
}