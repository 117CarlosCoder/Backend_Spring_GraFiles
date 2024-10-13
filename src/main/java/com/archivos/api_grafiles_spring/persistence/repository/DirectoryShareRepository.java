package com.archivos.api_grafiles_spring.persistence.repository;

import com.archivos.api_grafiles_spring.persistence.model.DirectoryShared;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DirectoryShareRepository extends MongoRepository<DirectoryShared, String> {
    List<DirectoryShared> findAllByUserIdAndIsDeletedFalse(ObjectId id);


    @Transactional
    @Query("{'_id' : ?0, 'user_id': ?1}")
    @Update("{'$set': {'isDeleted': true}}")
    void newDeletedByIdAndUser(ObjectId id, ObjectId id_user);
}
