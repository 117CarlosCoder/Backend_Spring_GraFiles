package com.archivos.api_grafiles_spring.service;

import com.archivos.api_grafiles_spring.controller.dto.EmployeeDTO;
import com.archivos.api_grafiles_spring.persistence.model.Employee;
import com.archivos.api_grafiles_spring.persistence.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public String createEmployee(EmployeeDTO employeeDTO){
        try {
            Employee employee = Employee.builder()
                    .name(employeeDTO.getEmpName())
                    .build();

            employeeRepository.save(employee);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "Employee created";
    }

    public List<Employee> getEmployee(){
        List<Employee> empList = new ArrayList<>();
        try {
            empList = employeeRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return empList;
    }

    public String deleteEmployee(String Id) {
        try {
            employeeRepository.deleteById(Id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "Employee Eliminated";
    }

    public String updateEmployee(EmployeeDTO employeeDTO) {
        try {
            Employee employee = Employee.builder()
                    .id(employeeDTO.getId())
                    .name(employeeDTO.getEmpName())
                    .build();

            employeeRepository.save(employee);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "Employee Update";
    }
}
