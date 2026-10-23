package com.example.l2_rbpo.dto;

import com.example.l2_rbpo.entity.UserRole;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        UserRole role,
        Instant createdAt
) {
}