package com.example.demo.service;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(UserRequest userRequest);

    UserResponse getUserById(UUID id);

    UserResponse getUserByUsername(String username);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(UUID id, UserRequest userRequest);

    void deleteUser(UUID id);

    UserResponse updateStatus(UUID id, User.UserStatus status);

    String login(String username, String password);
}
