package com.lazaro.taskmanager.service;

import com.lazaro.taskmanager.dto.TaskRequest;
import com.lazaro.taskmanager.dto.TaskResponse;
import com.lazaro.taskmanager.model.Task;
import com.lazaro.taskmanager.model.User;
import com.lazaro.taskmanager.repository.TaskRepository;
import com.lazaro.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public TaskResponse criar(TaskRequest request) {

        User dono = userRepository.buscarPorId(request.userId());
        if (dono == null) {
            throw new RuntimeException("Usuário responsável não encontrado.");
        }

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .userId(request.userId())
                .tags(request.tags() != null ? request.tags() : java.util.Collections.emptySet())
                .ttl(request.ttl())
                .build();

        Task salva = taskRepository.salvar(task);
        return new TaskResponse(salva);
    }

    public TaskResponse buscarPorId(String id) {
        Task task = taskRepository.buscarPorId(id);
        if (task == null) {
            throw new RuntimeException("Tarefa não encontrada.");
        }
        return new TaskResponse(task);
    }

    public List<TaskResponse> listarPorUsuario(String userId) {
        return taskRepository.buscarPorUsuario(userId).stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> listarTodas() {
        return taskRepository.buscarTodas().stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    public void deletar(String id) {
        Task task = taskRepository.buscarPorId(id);
        if (task == null) {
            throw new RuntimeException("Tarefa não encontrada.");
        }
        taskRepository.excluir(id);
    }
}