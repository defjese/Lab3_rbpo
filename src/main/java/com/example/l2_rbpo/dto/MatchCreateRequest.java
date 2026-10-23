package com.example.l2_rbpo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MatchCreateRequest(

        @NotNull(message = "Необходимо указать турнир")
        @Positive(message = "ID турнира должен быть положительным")
        Long tournamentId,

        @NotNull(message = "Необходимо указать первую команду")
        @Positive(message = "ID первой команды должен быть положительным")
        Long team1Id,

        @NotNull(message = "Необходимо указать вторую команду")
        @Positive(message = "ID второй команды должен быть положительным")
        Long team2Id

) {
}