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
    @Update("{'$set': {'name': ?1,'updated': new Date()}}")
    void updateNameByIdAndIsDeletedFalse(ObjectId id, String newName);

    List<Directory> findByDirectoryParentAndUserAndIsDeletedFalse(ObjectId directoryParentId, ObjectId user_id);

    List<Directory> findAllByUser(ObjectId user);

    Directory findByNameAndUserAndDirectoryAndDirectoryParentAndIsDeletedFalse(String name, ObjectId user, int directory, ObjectId directoryParent);

    Directory findByNameAndUserAndDirectoryAndIsDeletedFalse(String name, ObjectId user, int directory);

    Directory findByNameAndUser(String name, ObjectId user);

    Directory findByIdAndIsDeletedFalse(ObjectId id);

    Directory findByNameAndUserAndDirectoryParentAndIsDeletedFalse(String name, ObjectId user, ObjectId parent_id);

    @Transactional
    @Query("{'_id' : ?0, 'user': ?1}")
    @Update("{'$set': {'isDeleted': true}}")
    void newDeletedByIdAndUser(ObjectId id, ObjectId id_user);

    @Transactional
    @Query("{'_id' : ?0, 'user_id': ?1}")
    @Update("{'$set': {'directory_parent_id': ?2, directory:?3}}")
    void newupdateByIdAndDirectoryParentAndUserDirectoryAndAndIsDeletedFalse(ObjectId id,ObjectId id_user,ObjectId parent_id, int directory );
}

