package com.example.l2_rbpo.dto;

public record StandingResponse(
        Long teamId,
        String teamName,
        int played,
        int wins,
        int draws,
        int losses,
        int points
) {
}
