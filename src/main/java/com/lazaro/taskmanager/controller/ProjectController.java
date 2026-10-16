package com.lazaro.taskmanager.controller;

import com.lazaro.taskmanager.dto.ProjectRequest;
import com.lazaro.taskmanager.dto.ProjectResponse;
import com.lazaro.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> criar(@RequestBody @Valid ProjectRequest request) {
        ProjectResponse response = projectService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> buscarPorId(@PathVariable String id) {
        ProjectResponse response = projectService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<ProjectResponse>> listarPorUsuario(@PathVariable String userId) {
        List<ProjectResponse> response = projectService.listarPorUsuario(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        projectService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}