package com.example.l2_rbpo.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TeamPatchRequest(

        @Size(min = 1, max = 100, message = "Название должно содержать от 1 до 100 символов")
        String name,

        @Positive(message = "ID капитана должен быть положительным")
        Long captainId

) {
}