package com.archivos.api_grafiles_spring.persistence.repository;
import com.archivos.api_grafiles_spring.persistence.model.File;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FileRepository extends MongoRepository<File, ObjectId> {
    File findByGridFsFileId(ObjectId gridFsFileId);

    List<File> findAllByUserIdAndDirectoryIdAndIsDeletedFalse(ObjectId userId, ObjectId directoryId);

    @Transactional
    @Query("{'_id' : ?0, 'user_id': ?1}")
    @Update("{'$set': {'isDeleted': true}}")
    void newDeletedByIdAndUser(ObjectId id, ObjectId id_user);

    List<File> findAllByUserIdAndIsDeletedFalse(ObjectId id);

    List<File> findByDirectoryId(ObjectId directoryId);

    List<File> findByUserId(ObjectId userId);

    File findByIdAndName(ObjectId id, String name);

    File findByNameAndUserIdAndDirectoryIdAndIsDeletedFalse(String name, ObjectId user_id, ObjectId directory_id);

    File findByIdAndIsDeletedFalse(ObjectId fileId);

    @Transactional
    @Query("{'_id' : ?0, 'user_id': ?1}")
    @Update("{'$set': {'directory_id': ?2 }}")
    void newupdateByIdAndDirectoryParentAndUserDirectoryAndAndIsDeletedFalse(ObjectId id,ObjectId id_user,ObjectId parent_id);
}

