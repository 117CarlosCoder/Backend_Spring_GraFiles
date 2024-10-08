package com.archivos.api_grafiles_spring.persistence.repository;

import com.archivos.api_grafiles_spring.persistence.model.Directory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Repository
public interface DirectoryRepository extends MongoRepository<Directory,String> {

    @Query("{'_id' : ?0}")
    @Update("{'$set': {'name': ?1}}")
    void updateNameByIdAndIsDeletedFalse(ObjectId id, String newName);

    List<Directory> findAllByUser(ObjectId user);

    Directory findByNameAndUserAndDirectoryAndDirectoryParentAndIsDeletedFalse(String name, ObjectId user, int directory, ObjectId directoryParent);

    Directory findByNameAndUser(String name, ObjectId user);

    @Transactional
    @Query("{'_id' : ?0, 'user': ?1}")
    @Update("{'$set': {'isDeleted': true}}")  // Actualiza 'isDeleted' a true en lugar de borrar
    void newDeletedByIdAndUser(ObjectId id, ObjectId id_user);
}

