package com.archivos.api_grafiles_spring.persistence.repository;

import com.archivos.api_grafiles_spring.controller.dto.UserPasswordDTO;
import com.archivos.api_grafiles_spring.persistence.model.UserModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserModel,String> {
    Optional<UserModel> findUserEntityByUsername(String username);

    Optional<UserModel> findByEmail(String email);

    Optional<UserPasswordDTO> findById(ObjectId id);

    @Transactional
    @Query("{'_id' : ?0}")
    @Update("{'$set': {'password': ?1}}")
    void newPassword(ObjectId id, String password);

}
