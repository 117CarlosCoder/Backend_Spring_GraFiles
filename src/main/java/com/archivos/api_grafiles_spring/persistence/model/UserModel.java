package com.archivos.api_grafiles_spring.persistence.model;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(value = "user")
@Data
@Builder
public class UserModel {
    @Id
    private ObjectId id;
    @Field(name = "name")
    private String name;
    @Field(name = "username")
    private String username;
    @Field(name = "email")
    private String email;
    @Field(name = "password")
    private String password;
    @Field(name = "is_enabled")
    private boolean isEnabled;
    @Field(name = "account_No_Expired")
    private boolean accountNoExpired;
    @Field(name = "account_No_Locked")
    private boolean accountNoLocked;
    @Field(name = "credential_No_Expired")
    private boolean credentialNoExpired;
    @Field(name = "update")
    private boolean update;
    @Field(name = "role")
    private RoleEnum role;
}
