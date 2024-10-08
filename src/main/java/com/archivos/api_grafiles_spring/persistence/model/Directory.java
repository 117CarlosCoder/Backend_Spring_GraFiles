package com.archivos.api_grafiles_spring.persistence.model;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document(collection = "directory")
@Data
@Builder
public class Directory {
    @Id
    private ObjectId id;
    @Field(name = "name")
    private String name;
    @Field(name = "user_id")
    private ObjectId user;
    @Field(name = "directory")
    private int directory;
    @Field(name = "directory_parent_id")
    private ObjectId directoryParent;
    @Field(name = "isDeleted")
    private boolean isDeleted;
    @Field(name = "created")
    private Date created;
    @Field(name = "updated")
    private Date updated;
}
