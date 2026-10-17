package com.lazaro.taskmanager.repository;

import com.lazaro.taskmanager.model.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserRepository {


    private final StringRedisTemplate valkey;

    private static final String KEY_PREFIX = "user:";
    private static final String ALL_USERS_SET = "users:all";
    private static final String EMAIL_INDEX_PREFIX = "user:email:";

    public UserRepository(StringRedisTemplate valkey) {
        this.valkey = valkey;
    }

    public User salvar(User user) {
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(UUID.randomUUID().toString());
        }

        String key = KEY_PREFIX + user.getId();

        Map<String, String> hash = new HashMap<>();
        hash.put("id", user.getId());
        hash.put("name", user.getName());
        hash.put("email", user.getEmail());
        hash.put("password", user.getPassword() != null ? user.getPassword() : "");
        valkey.opsForHash().putAll(key, hash);


        valkey.opsForSet().add(ALL_USERS_SET, user.getId());


        valkey.opsForValue().set(EMAIL_INDEX_PREFIX + user.getEmail(), user.getId());

        return user;
    }

    public User buscarPorId(String id) {
        String key = KEY_PREFIX + id;
        Map<Object, Object> entries = valkey.opsForHash().entries(key);

        if (entries.isEmpty()) {
            return null;
        }

        return User.builder()
                .id((String) entries.get("id"))
                .name((String) entries.get("name"))
                .email((String) entries.get("email"))
                .password((String) entries.get("password"))
                .build();
    }

    public User buscarPorEmail(String email) {
        String userId = valkey.opsForValue().get(EMAIL_INDEX_PREFIX + email); // comando GET
        if (userId == null) {
            return null;
        }
        return buscarPorId(userId);
    }

    public List<User> buscarTodos() {
        Set<String> ids = valkey.opsForSet().members(ALL_USERS_SET); // comando SMEMBERS
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> users = new ArrayList<>();
        for (String id : ids) {
            User user = buscarPorId(id);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }
    // Incrementa o contador de forma atômica (O(1))
    public void incrementarTarefasConcluidas(String userId) {
        valkey.opsForValue().increment("user:" + userId + ":stats:completed");
    }

    // Lê o valor atual do contador
    public int buscarTarefasConcluidas(String userId) {
        String count = valkey.opsForValue().get("user:" + userId + ":stats:completed");
        return count != null ? Integer.parseInt(count) : 0;
    }

    public void atualizar(User user) {
        salvar(user);
    }

    public void excluir(String id) {
        User user = buscarPorId(id);
        if (user != null) {
            valkey.delete(KEY_PREFIX + id);
            valkey.opsForSet().remove(ALL_USERS_SET, id);
            valkey.delete(EMAIL_INDEX_PREFIX + user.getEmail());
        }
    }
}