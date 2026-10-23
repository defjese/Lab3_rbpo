package com.example.l2_rbpo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

        @NotBlank(message = "Логин обязателен")
        @Size(min = 3, max = 50, message = "Логин должен содержать от 3 до 50 символов")
        String username,

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 12, max = 72, message = "Пароль должен содержать от 12 до 72 символов")
        String password,

        @NotBlank(message = "Имя обязательно")
        @Size(max = 100, message = "Имя не должно превышать 100 символов")
        String displayName

) {
}