package com.lazaro.taskmanager.model;

import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private String id;
    private String title;
    private String description;

    @Builder.Default
    private StatusTarefa status = StatusTarefa.PENDENTE;

    private String userId;

    @Builder.Default
    private Set<String> tags = new HashSet<>();

    private Long ttl;
}