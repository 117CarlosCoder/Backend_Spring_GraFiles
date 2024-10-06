package com.archivos.api_grafiles_spring.persistence.repository;

import com.archivos.api_grafiles_spring.persistence.model.UserModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserModel,String> {
    Optional<UserModel> findUserEntityByUsername(String username);
}
