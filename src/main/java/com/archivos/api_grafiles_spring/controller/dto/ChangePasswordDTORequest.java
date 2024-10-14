package com.archivos.api_grafiles_spring.controller.dto;

import jakarta.validation.constraints.NotBlank;


public record ChangePasswordDTORequest(
        @NotBlank String actualPassword,
        @NotBlank String newPassword){
}