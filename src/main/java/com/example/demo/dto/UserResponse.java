package com.example.demo.dto;

import com.example.demo.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String email,
    User.UserStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
