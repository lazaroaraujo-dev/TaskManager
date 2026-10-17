package com.lazaro.taskmanager.service;

import com.lazaro.taskmanager.dto.UserRequest;
import com.lazaro.taskmanager.dto.UserResponse;
import com.lazaro.taskmanager.model.User;
import com.lazaro.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse criar(UserRequest request) {

        User existente = userRepository.buscarPorEmail(request.email());
        if (existente != null) {
            throw new RuntimeException("Já existe um usuário cadastrado com este e-mail.");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password())
                .build();

        User salvo = userRepository.salvar(user);
        return new UserResponse(salvo);
    }

    public UserResponse buscarPorId(String id) {
        User user = userRepository.buscarPorId(id);
        if (user == null) {
            throw new RuntimeException("Usuário não encontrado.");
        }
        return new UserResponse(user);
    }
    public int buscarTarefasConcluidas(String userId) {
        // Valida se o usuário existe antes de buscar a estatística
        User user = userRepository.buscarPorId(userId);
        if (user == null) {
            throw new RuntimeException("Usuário não encontrado.");
        }

        // Faz o repasse para o repositório que busca a chave no Valkey
        return userRepository.buscarTarefasConcluidas(userId);
    }

    public List<UserResponse> listarTodos() {
        return userRepository.buscarTodos().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    public void deletar(String id) {
        UserResponse user = buscarPorId(id); // Garante que existe
        userRepository.excluir(user.id());
    }
}