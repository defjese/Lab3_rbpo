package com.example.l2_rbpo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TeamCreateRequest(

        @NotBlank(message = "Название команды обязательно")
        @Size(max = 100, message = "Название не должно превышать 100 символов")
        String name,

        @NotNull(message = "Необходимо указать капитана")
        @Positive(message = "ID капитана должен быть положительным")
        Long captainId

) {
}