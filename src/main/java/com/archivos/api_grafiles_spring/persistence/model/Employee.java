package com.archivos.api_grafiles_spring.persistence.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(value = "employee")
@Data
@Builder
public class Employee {
    @Id
    private String id;
    @Field(name = "employee_name")
    private String name ;

}
