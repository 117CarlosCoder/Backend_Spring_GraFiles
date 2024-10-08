package com.archivos.api_grafiles_spring.controller.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DirectoryDTOResquest {
    private String name;
    private int directory;
    private String directory_parent_id;

}
