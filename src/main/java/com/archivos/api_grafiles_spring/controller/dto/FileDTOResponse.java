package com.archivos.api_grafiles_spring.controller.dto;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class FileDTOResponse {
    private String Id;
    private String name;
    private String directory_id;
    private String userShared;
    private long size;
    private String fileType;
    private Date created;
    private Date updated;
    private byte[] content;
}
