package com.lazaro.taskmanager.controller;

import com.lazaro.taskmanager.dto.UserRequest;
import com.lazaro.taskmanager.dto.UserResponse;
import com.lazaro.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> criar(@RequestBody @Valid UserRequest request) {
        UserResponse response = userService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> buscarPorId(@PathVariable String id) {
        UserResponse response = userService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listarTodos() {
        List<UserResponse> response = userService.listarTodos();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        userService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}