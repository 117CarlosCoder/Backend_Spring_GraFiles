package com.archivos.api_grafiles_spring.controller;

import com.archivos.api_grafiles_spring.controller.dto.EmployeeDTO;
import com.archivos.api_grafiles_spring.persistence.model.Employee;
import com.archivos.api_grafiles_spring.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emp")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public String createEmployee(@RequestBody EmployeeDTO emp){
        return employeeService.createEmployee(emp);
    }

    @GetMapping("/get/employee")
    @ResponseStatus(HttpStatus.OK)
    public List<Employee> getEmployee(){
        return employeeService.getEmployee();
    }

    @PutMapping("/edit/employee")
    @ResponseStatus(HttpStatus.OK)
    public String editEmployee(@RequestBody EmployeeDTO emp){
        return employeeService.updateEmployee(emp);
    }

    @DeleteMapping("/delete/employee")
    @ResponseStatus(HttpStatus.OK)
    public String deleteEmployee(@RequestParam String Id){
        return employeeService.deleteEmployee(Id);
    }

}
