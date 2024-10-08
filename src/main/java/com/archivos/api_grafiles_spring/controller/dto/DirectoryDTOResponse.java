package com.archivos.api_grafiles_spring.controller.dto;

import com.archivos.api_grafiles_spring.persistence.model.Directory;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class DirectoryDTOResponse {
    private String Id;
    private String name;
    private int directory;
    private String directory_parent_id;
    private Date created;
    private Date updated;

}
