package com.example.l2_rbpo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TeamApplicationCreateRequest(

        @NotNull(message = "Необходимо указать команду")
        @Positive(message = "ID команды должен быть положительным")
        Long teamId,

        @NotNull(message = "Необходимо указать турнир")
        @Positive(message = "ID турнира должен быть положительным")
        Long tournamentId

) {
}