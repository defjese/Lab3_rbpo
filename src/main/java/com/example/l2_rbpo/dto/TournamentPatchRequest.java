package com.example.l2_rbpo.dto;

import com.example.l2_rbpo.entity.TournamentStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TournamentPatchRequest(

        @Size(min = 1, max = 100, message = "Название должно содержать от 1 до 100 символов")
        String name,

        @Positive(message = "Лимит команд должен быть больше нуля")
        Integer teamLimit,

        TournamentStatus status

) {
}