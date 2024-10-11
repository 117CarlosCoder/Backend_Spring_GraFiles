package com.archivos.api_grafiles_spring.controller.dto;

import lombok.*;

import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DirectoryCopiDTORequest {
    private String Id;
    private String directory_parent_id;
}
