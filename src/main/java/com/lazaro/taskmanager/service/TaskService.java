package com.lazaro.taskmanager.service;

import com.lazaro.taskmanager.dto.TaskRequest;
import com.lazaro.taskmanager.dto.TaskResponse;
import com.lazaro.taskmanager.model.Task;
import com.lazaro.taskmanager.model.StatusTarefa;
import com.lazaro.taskmanager.model.User;
import com.lazaro.taskmanager.repository.ProjectRepository;
import com.lazaro.taskmanager.repository.TaskRepository;
import com.lazaro.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    public TaskResponse criar(TaskRequest request) {

        // 1. Valida se o utilizador existe
        User dono = userRepository.buscarPorId(request.userId());
        if (dono == null) {
            throw new RuntimeException("Usuário responsável não encontrado.");
        }

        // 2. Valida se o projeto existe (caso tenha sido enviado no request)
        if (request.projectId() != null && !request.projectId().isBlank()) {
            if (projectRepository.buscarPorId(request.projectId()) == null) {
                throw new RuntimeException("Projeto não encontrado.");
            }
        }

        // 3. Constrói a Tarefa mapeando todos os campos do Request
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .userId(request.userId())
                .projectId(request.projectId())
                .tags(request.tags() != null ? request.tags() : Collections.emptySet())
                .ttl(request.ttl())
                .build(); // Faltava fechar o builder

        // 4. Salva no Valkey e devolve a resposta formatada
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

    // NOVO: Método para listar tarefas de um projeto específico
    public List<TaskResponse> listarPorProjeto(String projectId) {
        return taskRepository.buscarPorProjeto(projectId).stream()
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
    public TaskResponse concluir(String id) {
        Task task = taskRepository.buscarPorId(id);
        if (task == null) {
            throw new RuntimeException("Tarefa não encontrada.");
        }

        task.setStatus(com.lazaro.taskmanager.model.StatusTarefa.CONCLUIDA);
        taskRepository.atualizar(task);

        //  Dispara o contador atômico no Valkey
        userRepository.incrementarTarefasConcluidas(task.getUserId());

        return new TaskResponse(task);
    }
}