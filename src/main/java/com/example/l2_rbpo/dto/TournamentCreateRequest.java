package com.example.l2_rbpo.dto;

import com.example.l2_rbpo.entity.TournamentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TournamentCreateRequest(

        @NotBlank(message = "Название турнира обязательно")
        @Size(max = 100, message = "Название не должно превышать 100 символов")
        String name,

        @NotNull(message = "Лимит команд обязателен")
        @Positive(message = "Лимит команд должен быть больше нуля")
        Integer teamLimit,

        @NotNull(message = "Статус турнира обязателен")
        TournamentStatus status

) {
}