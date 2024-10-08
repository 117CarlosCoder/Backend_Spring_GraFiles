package com.archivos.api_grafiles_spring.controller.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class UpdateDirectoryDTORequest {
    private String id;
    private String name;
}
