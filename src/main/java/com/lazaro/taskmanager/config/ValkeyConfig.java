package com.lazaro.taskmanager.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ValkeyConfig {
    // O Spring Boot já injeta o StringRedisTemplate automaticamente
    // usando o host e port definidos no seu application.yml!
}