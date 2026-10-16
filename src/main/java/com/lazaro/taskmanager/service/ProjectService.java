package com.lazaro.taskmanager.service;

import com.lazaro.taskmanager.dto.ProjectRequest;
import com.lazaro.taskmanager.dto.ProjectResponse;
import com.lazaro.taskmanager.model.Project;
import com.lazaro.taskmanager.model.User;
import com.lazaro.taskmanager.repository.ProjectRepository;
import com.lazaro.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public ProjectResponse criar(ProjectRequest request) {
        User dono = userRepository.buscarPorId(request.userId());
        if (dono == null) {
            throw new RuntimeException("Utilizador não encontrado.");
        }

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .userId(request.userId())
                .build();

        return new ProjectResponse(projectRepository.salvar(project));
    }
    public ProjectResponse buscarPorId(String id) {
        Project project = projectRepository.buscarPorId(id);
        if (project == null) {
            throw new RuntimeException("Projeto não encontrado.");
        }
        return new ProjectResponse(project);
    }

    public List<ProjectResponse> listarPorUsuario(String userId) {
        return projectRepository.buscarPorUsuario(userId).stream()
                .map(ProjectResponse::new)
                .collect(Collectors.toList());
    }

    public void deletar(String id) {
        Project project = projectRepository.buscarPorId(id);
        if (project == null) {
            throw new RuntimeException("Projeto não encontrado.");
        }
        projectRepository.excluir(id);
    }
}