package com.example.l2_rbpo.dto;

import jakarta.validation.constraints.Size;

public record UserPatchRequest(

        @Size(min = 3, max = 50, message = "Логин должен содержать от 3 до 50 символов")
        String username,

        @Size(min = 12, max = 72, message = "Пароль должен содержать от 12 до 72 символов")
        String password,

        @Size(min = 1, max = 100, message = "Имя должно содержать от 1 до 100 символов")
        String displayName

) {
}