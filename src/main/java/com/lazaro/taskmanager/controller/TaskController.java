package com.lazaro.taskmanager.controller;

import com.lazaro.taskmanager.dto.TaskRequest;
import com.lazaro.taskmanager.dto.TaskResponse;
import com.lazaro.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> criar(@RequestBody @Valid TaskRequest request) {
        TaskResponse response = taskService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> buscarPorId(@PathVariable String id) {
        TaskResponse response = taskService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<TaskResponse>> listarPorUsuario(@PathVariable String userId) {
        List<TaskResponse> response = taskService.listarPorUsuario(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listarTodas() {
        List<TaskResponse> response = taskService.listarTodas();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        taskService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}