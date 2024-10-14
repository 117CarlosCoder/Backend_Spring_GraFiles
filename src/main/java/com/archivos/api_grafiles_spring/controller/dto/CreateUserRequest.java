package com.archivos.api_grafiles_spring.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank String password) {
}
