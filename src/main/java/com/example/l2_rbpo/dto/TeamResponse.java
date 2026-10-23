package com.example.l2_rbpo.dto;

public record TeamResponse(
        Long id,
        String name,
        Long captainId,
        String captainUsername
) {
}