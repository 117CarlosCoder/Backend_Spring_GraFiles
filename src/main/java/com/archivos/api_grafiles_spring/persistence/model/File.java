package com.archivos.api_grafiles_spring.persistence.model;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document(collection = "file")
@Data
@Builder
public class File {

    @Id
    private ObjectId id;

    @Field(name = "name")
    private String name;

    @Field(name = "directory_id")
    private ObjectId directoryId;

    @Field(name = "user_id")
    private ObjectId userId;

    @Field(name = "size")
    private long size;

    @Field(name = "file_type")
    private String fileType;

    @Field(name = "gridfs_file_id")
    private ObjectId gridFsFileId;

    @Field(name = "isDeleted")
    private boolean isDeleted;

    @Field(name = "created")
    private Date created;

    @Field(name = "updated")
    private Date updated;
}
