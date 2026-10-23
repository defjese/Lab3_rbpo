package com.example.l2_rbpo.dto;

import com.example.l2_rbpo.entity.ApplicationStatus;

public record TeamApplicationResponse(
        Long id,
        Long teamId,
        String teamName,
        Long tournamentId,
        String tournamentName,
        ApplicationStatus status
) {
}