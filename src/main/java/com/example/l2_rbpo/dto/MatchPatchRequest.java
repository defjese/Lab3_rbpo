package com.example.l2_rbpo.dto;

import com.example.l2_rbpo.entity.MatchStatus;
import jakarta.validation.constraints.PositiveOrZero;

public record MatchPatchRequest(

        @PositiveOrZero(message = "Счёт первой команды не может быть отрицательным")
        Integer team1Score,

        @PositiveOrZero(message = "Счёт второй команды не может быть отрицательным")
        Integer team2Score,

        MatchStatus status

) {
}