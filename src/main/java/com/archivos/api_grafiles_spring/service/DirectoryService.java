package com.archivos.api_grafiles_spring.service;

import com.archivos.api_grafiles_spring.controller.dto.DirectoryDTOResponse;
import com.archivos.api_grafiles_spring.controller.dto.DirectoryDTOResquest;
import com.archivos.api_grafiles_spring.controller.dto.UpdateDirectoryDTORequest;
import com.archivos.api_grafiles_spring.persistence.model.Directory;
import com.archivos.api_grafiles_spring.persistence.repository.DirectoryInfoRepository;
import com.archivos.api_grafiles_spring.persistence.repository.DirectoryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DirectoryService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private DirectoryInfoRepository directoryInfoRepository;

    public String createDirectory(DirectoryDTOResquest directoryDTOResquest, String id){
        try {
            if (getDirectoryExist(directoryDTOResquest, id) != null){
                System.out.println("Directorio ya existente");
                throw new RuntimeException("Directorio ya existente");
            }else {
                Directory directory = Directory.builder()
                        .name(directoryDTOResquest.getName())
                        .user(new ObjectId(id))
                        .directory(directoryDTOResquest.getDirectory())
                        .directoryParent(new ObjectId(directoryDTOResquest.getDirectory_parent_id()))
                        .isDeleted(false)
                        .created(new Date())
                        .updated(new Date())
                        .build();

                directoryRepository.save(directory);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return "Directorio Creado";
    }

    public String updateDirectory(UpdateDirectoryDTORequest updateDirectoryDTORequest, String id){
        try {
                directoryRepository.updateNameByIdAndIsDeletedFalse(new ObjectId(updateDirectoryDTORequest.getId()),updateDirectoryDTORequest.getName());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return "Directorio Actualizado";
    }

    public String deleteDirectory(String id, String id_user){
        try {
            directoryRepository.newDeletedByIdAndUser(new ObjectId(id), new ObjectId(id_user));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return "Directorio Eliminado";
    }

    public List<DirectoryDTOResponse> getDirectorys(String id, String user_id) {
        List<Directory> directories = directoryInfoRepository.findALLByDirectoryParentAndUserAndIsDeletedFalse(new ObjectId(id), new ObjectId(user_id));
        
        return directories.stream().map(directory -> new DirectoryDTOResponse(
                directory.getId().toHexString(),
                directory.getName(),
                directory.getDirectory(),
                directory.getDirectoryParent().toHexString(),
                directory.getCreated(),
                directory.getUpdated()
        )).collect(Collectors.toList());
    }

    public Directory getDirectoryExist(DirectoryDTOResquest directoryDTOResquest, String user) {
        return directoryRepository.findByNameAndUserAndDirectoryAndDirectoryParentAndIsDeletedFalse(directoryDTOResquest.getName(), new ObjectId(user),directoryDTOResquest.getDirectory(), new ObjectId(directoryDTOResquest.getDirectory_parent_id()));
    }


    public DirectoryDTOResquest getDirectory(DirectoryDTOResquest directoryDTOResquest, String user){
        Directory directory = directoryRepository.findByNameAndUserAndDirectoryAndDirectoryParentAndIsDeletedFalse(directoryDTOResquest.getName(), new ObjectId(user),directoryDTOResquest.getDirectory(), new ObjectId(directoryDTOResquest.getDirectory_parent_id()));
        return new DirectoryDTOResquest(directory.getName(), directory.getDirectory(), directory.getDirectoryParent().toHexString());
    }
}
