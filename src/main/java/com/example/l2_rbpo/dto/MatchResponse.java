package com.example.l2_rbpo.dto;

import com.example.l2_rbpo.entity.MatchStatus;

public record MatchResponse(
        Long id,
        Long tournamentId,
        Long team1Id,
        String team1Name,
        Long team2Id,
        String team2Name,
        Integer team1Score,
        Integer team2Score,
        MatchStatus status
) {
}