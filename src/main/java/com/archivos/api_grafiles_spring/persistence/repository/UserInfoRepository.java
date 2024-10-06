package com.archivos.api_grafiles_spring.persistence.repository;

import com.archivos.api_grafiles_spring.controller.dto.UserInfoResponse;
import com.archivos.api_grafiles_spring.persistence.model.UserModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserInfoRepository extends MongoRepository<UserInfoResponse,String> {
    Optional<UserInfoResponse> findById(String id);
}
