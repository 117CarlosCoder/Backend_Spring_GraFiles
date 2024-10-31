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
    private int directory;
    private String directory_parent_id;

}
