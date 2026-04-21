package com.example.demo.dto;

public record JwtResponse(
        String token,
        String type,
        String username,
        String email
) {
}
