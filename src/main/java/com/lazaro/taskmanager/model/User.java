package com.lazaro.taskmanager.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String name;
    private String email;

    @ToString.Exclude
    private String password;
}