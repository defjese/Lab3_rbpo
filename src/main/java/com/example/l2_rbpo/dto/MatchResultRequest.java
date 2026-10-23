package com.example.l2_rbpo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MatchResultRequest(

        @NotNull(message = "Необходимо указать счёт первой команды")
        @PositiveOrZero(message = "Счёт первой команды не может быть отрицательным")
        Integer team1Score,

        @NotNull(message = "Необходимо указать счёт второй команды")
        @PositiveOrZero(message = "Счёт второй команды не может быть отрицательным")
        Integer team2Score

) {
}
