package com.archivos.api_grafiles_spring.controller.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class UserInfoResponse {
    private String name;
    private String email;
}
