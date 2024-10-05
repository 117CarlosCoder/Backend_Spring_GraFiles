package com.archivos.api_grafiles_spring.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EmployeeDTO {
    private String id;
    private String empName ;
    private String loction;
    private BigDecimal salary;
}
