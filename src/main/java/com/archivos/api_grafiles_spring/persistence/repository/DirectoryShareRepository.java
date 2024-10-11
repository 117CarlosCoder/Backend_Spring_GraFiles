package com.archivos.api_grafiles_spring.persistence.repository;

import com.archivos.api_grafiles_spring.persistence.model.DirectoryShared;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DirectoryShareRepository extends MongoRepository<DirectoryShared, String> {
    List<DirectoryShared> findAllByUserIdAndIsDeletedFalse(ObjectId id);
}
