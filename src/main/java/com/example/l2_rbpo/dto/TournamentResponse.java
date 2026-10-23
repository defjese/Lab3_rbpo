package com.example.l2_rbpo.dto;

import com.example.l2_rbpo.entity.TournamentStatus;

public record TournamentResponse(
        Long id,
        String name,
        Integer teamLimit,
        TournamentStatus status
) {
}