package com.archivos.api_grafiles_spring.persistence.repository;

import com.archivos.api_grafiles_spring.controller.dto.DirectoryDTOResponse;
import com.archivos.api_grafiles_spring.controller.dto.DirectoryDTOResquest;
import com.archivos.api_grafiles_spring.persistence.model.Directory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectoryInfoRepository extends MongoRepository<Directory,String> {
    List<Directory> findALLByDirectoryParentAndUserAndIsDeletedFalse(ObjectId id, ObjectId user);
}
