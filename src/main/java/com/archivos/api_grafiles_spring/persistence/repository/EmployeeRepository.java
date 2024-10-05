package com.archivos.api_grafiles_spring.persistence.repository;

import com.archivos.api_grafiles_spring.persistence.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {

}
