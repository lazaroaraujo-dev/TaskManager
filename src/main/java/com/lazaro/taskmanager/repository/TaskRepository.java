package com.lazaro.taskmanager.repository;

import com.lazaro.taskmanager.model.StatusTarefa;
import com.lazaro.taskmanager.model.Task;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.*;

@Repository
public class TaskRepository {

    private final StringRedisTemplate valkey;

    private static final String KEY_PREFIX = "task:";
    private static final String ALL_TASKS_SET = "tasks:all";

    public TaskRepository(StringRedisTemplate valkey) {
        this.valkey = valkey;
    }

    public Task salvar(Task task) {
        if (task.getId() == null || task.getId().isBlank()) {
            task.setId(UUID.randomUUID().toString());
        }

        String key = KEY_PREFIX + task.getId();

        Map<String, String> hash = new HashMap<>();
        hash.put("id", task.getId());
        hash.put("title", task.getTitle());
        hash.put("description", task.getDescription() != null ? task.getDescription() : "");
        hash.put("status", task.getStatus().name());
        hash.put("userId", task.getUserId());
        hash.put("tags", String.join(",", task.getTags()));

        valkey.opsForHash().putAll(key, hash);


        valkey.opsForSet().add(ALL_TASKS_SET, task.getId());
        valkey.opsForSet().add("user:" + task.getUserId() + ":tasks", task.getId());


        if (task.getTtl() != null && task.getTtl() > 0) {
            valkey.expire(key, Duration.ofSeconds(task.getTtl()));
        }

        return task;
    }

    public Task buscarPorId(String id) {
        String key = KEY_PREFIX + id;
        Map<Object, Object> entries = valkey.opsForHash().entries(key);

        if (entries.isEmpty()) {
            return null;
        }

        String tagsStr = (String) entries.get("tags");
        Set<String> tags = (tagsStr == null || tagsStr.isBlank())
                ? new HashSet<>()
                : new HashSet<>(Arrays.asList(tagsStr.split(",")));

        Long remainingTtl = valkey.getExpire(key);

        return Task.builder()
                .id((String) entries.get("id"))
                .title((String) entries.get("title"))
                .description((String) entries.get("description"))
                .status(StatusTarefa.valueOf((String) entries.get("status")))
                .userId((String) entries.get("userId"))
                .tags(tags)
                .ttl(remainingTtl != null && remainingTtl > 0 ? remainingTtl : null)
                .build();
    }

    public List<Task> buscarPorUsuario(String userId) {
        Set<String> taskIds = valkey.opsForSet().members("user:" + userId + ":tasks");
        if (taskIds == null || taskIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Task> tasks = new ArrayList<>();
        for (String taskId : taskIds) {
            Task task = buscarPorId(taskId);
            if (task != null) {
                tasks.add(task);
            } else {

                valkey.opsForSet().remove("user:" + userId + ":tasks", taskId);
            }
        }
        return tasks;
    }

    public List<Task> buscarTodas() {
        Set<String> ids = valkey.opsForSet().members(ALL_TASKS_SET);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Task> tasks = new ArrayList<>();
        for (String id : ids) {
            Task task = buscarPorId(id);
            if (task != null) {
                tasks.add(task);
            } else {
                valkey.opsForSet().remove(ALL_TASKS_SET, id);
            }
        }
        return tasks;
    }

    public void atualizar(Task task) {
        salvar(task);
    }

    public void excluir(String id) {
        Task task = buscarPorId(id);
        if (task != null) {
            valkey.delete(KEY_PREFIX + id);
            valkey.opsForSet().remove(ALL_TASKS_SET, id);
            valkey.opsForSet().remove("user:" + task.getUserId() + ":tasks", id);
        }
    }
}