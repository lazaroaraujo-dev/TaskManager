package com.lazaro.taskmanager.repository;

import com.lazaro.taskmanager.model.Project;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ProjectRepository {

    private final StringRedisTemplate valkey;
    private static final String KEY_PREFIX = "project:";
    private static final String ALL_PROJECTS_SET = "projects:all";

    public ProjectRepository(StringRedisTemplate valkey) {
        this.valkey = valkey;
    }

    public Project salvar(Project project) {
        if (project.getId() == null || project.getId().isBlank()) {
            project.setId(UUID.randomUUID().toString());
        }

        String key = KEY_PREFIX + project.getId();
        Map<String, String> hash = new HashMap<>();
        hash.put("id", project.getId());
        hash.put("name", project.getName());
        hash.put("description", project.getDescription() != null ? project.getDescription() : "");
        hash.put("userId", project.getUserId());

        valkey.opsForHash().putAll(key, hash);
        valkey.opsForSet().add(ALL_PROJECTS_SET, project.getId());
        valkey.opsForSet().add("user:" + project.getUserId() + ":projects", project.getId());

        return project;
    }

    public Project buscarPorId(String id) {
        Map<Object, Object> entries = valkey.opsForHash().entries(KEY_PREFIX + id);
        if (entries.isEmpty()) return null;

        return Project.builder()
                .id((String) entries.get("id"))
                .name((String) entries.get("name"))
                .description((String) entries.get("description"))
                .userId((String) entries.get("userId"))
                .build();
    }

    public List<Project> buscarPorUsuario(String userId) {
        // Busca os IDs dos projetos que pertencem a este usuário no Set
        Set<String> projectIds = valkey.opsForSet().members("user:" + userId + ":projects");
        if (projectIds == null || projectIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Project> projects = new ArrayList<>();
        for (String id : projectIds) {
            Project p = buscarPorId(id);
            if (p != null) {
                projects.add(p);
            } else {
                // Limpeza caso o projeto tenha sido deletado incorretamente
                valkey.opsForSet().remove("user:" + userId + ":projects", id);
            }
        }
        return projects;
    }

    public List<Project> buscarTodos() {
        Set<String> ids = valkey.opsForSet().members(ALL_PROJECTS_SET);
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<Project> projects = new ArrayList<>();
        for (String id : ids) {
            Project p = buscarPorId(id);
            if (p != null) projects.add(p);
        }
        return projects;
    }
    public void excluir(String id) {
        Project p = buscarPorId(id);
        if (p != null) {
            valkey.delete(KEY_PREFIX + id);
            valkey.opsForSet().remove(ALL_PROJECTS_SET, id);
            valkey.opsForSet().remove("user:" + p.getUserId() + ":projects", id);
        }
    }
}