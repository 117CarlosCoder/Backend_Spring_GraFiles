package com.archivos.api_grafiles_spring.service;

import com.archivos.api_grafiles_spring.controller.dto.DirectoryDTOResponse;
import com.archivos.api_grafiles_spring.controller.dto.DirectoryDTOResquest;
import com.archivos.api_grafiles_spring.controller.dto.UpdateDirectoryDTORequest;
import com.archivos.api_grafiles_spring.persistence.model.Directory;
import com.archivos.api_grafiles_spring.persistence.model.File;
import com.archivos.api_grafiles_spring.persistence.repository.DirectoryInfoRepository;
import com.archivos.api_grafiles_spring.persistence.repository.DirectoryRepository;
import com.archivos.api_grafiles_spring.persistence.repository.FileRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DirectoryService {

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private DirectoryInfoRepository directoryInfoRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    public int count = 0;


    public ResponseEntity<String> createDirectory(DirectoryDTOResquest directoryDTOResquest, String id){
        try {
            if (getDirectoryExist(directoryDTOResquest, id) != null){
                System.out.println("Directorio ya existente");
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El directorio ya existe.");
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
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el directorio: " + e.getMessage());
        }
        return  ResponseEntity.status(HttpStatus.CREATED).body("Directorio creado exitosamente.");
    }

    public ResponseEntity<String> updateDirectory(UpdateDirectoryDTORequest updateDirectoryDTORequest, String id){
        try {
            if (getDirectoryExist(updateDirectoryDTORequest, id) != null){
                System.out.println("Directorio ya existente");
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El directorio ya existe.");
            }else{
                directoryRepository.updateNameByIdAndIsDeletedFalse(new ObjectId(updateDirectoryDTORequest.getId()),updateDirectoryDTORequest.getName());
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return  ResponseEntity.status(HttpStatus.OK).body("Directorio Actualizado.");
    }


    public String deleteDirectory(String id, String id_user) {
        try {
            Directory directory = directoryRepository.findByIdAndIsDeletedFalse(new ObjectId(id));

            if (directory == null) {
                throw new RuntimeException("Directorio no encontrado o ya eliminado.");
            }

            deleteDirectoryRecursively(directory.getId(), id_user);

            return "Directorio y su contenido eliminado correctamente";
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el directorio: " + e.getMessage(), e);
        }
    }

    private void deleteDirectoryRecursively(ObjectId directoryId, String id_user) {

        List<File> filesInDirectory = fileRepository.findAllByUserIdAndDirectoryIdAndIsDeletedFalse(new ObjectId(id_user), directoryId);

        for (File file : filesInDirectory) {
            deleteFile(file);
        }

        List<Directory> subdirectories = directoryRepository.findByDirectoryParentAndUserAndIsDeletedFalse(directoryId, new ObjectId(id_user));

        for (Directory subdirectory : subdirectories) {
            deleteDirectoryRecursively(subdirectory.getId(), id_user);
        }

        directoryRepository.newDeletedByIdAndUser(directoryId, new ObjectId(id_user));
    }

    private void deleteFile(File file) {
        try {



            file.setDeleted(true);
            file.setUpdated(new Date());
            fileRepository.save(file);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el archivo: " + file.getName(), e);
        }
    }


    public ResponseEntity<List<DirectoryDTOResponse>> getDirectorys(String id, String user_id) {
            List<DirectoryDTOResponse> directoryDTOResponses = List.of();
        try {
            List<Directory> directories = directoryInfoRepository.findALLByDirectoryParentAndUserAndIsDeletedFalse(new ObjectId(id), new ObjectId(user_id));

             directoryDTOResponses = directories.stream().map(directory -> new DirectoryDTOResponse(
                    directory.getId().toHexString(),
                    directory.getName(),
                    directory.getDirectory(),
                    directory.getDirectoryParent().toHexString(),
                    directory.getCreated(),
                    directory.getUpdated()
            )).collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.OK).body(directoryDTOResponses);

        }catch (Exception e){
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(directoryDTOResponses);
        }


    }

    public Directory getDirectoryExist(DirectoryDTOResquest directoryDTOResquest, String user) {
        return directoryRepository.findByNameAndUserAndDirectoryAndDirectoryParentAndIsDeletedFalse(directoryDTOResquest.getName(), new ObjectId(user),directoryDTOResquest.getDirectory(), new ObjectId(directoryDTOResquest.getDirectory_parent_id()));
    }

    public Directory getDirectoryExist( UpdateDirectoryDTORequest updateDirectoryDTORequest, String user) {
        return directoryRepository.findByNameAndUserAndDirectoryAndDirectoryParentAndIsDeletedFalse(updateDirectoryDTORequest.getName(), new ObjectId(user), updateDirectoryDTORequest.getDirectory(),new ObjectId(updateDirectoryDTORequest.getDirectory_parent_id()));
    }


    public DirectoryDTOResquest getDirectory(DirectoryDTOResquest directoryDTOResquest, String user){
        Directory directory = directoryRepository.findByNameAndUserAndDirectoryAndDirectoryParentAndIsDeletedFalse(directoryDTOResquest.getName(), new ObjectId(user),directoryDTOResquest.getDirectory(), new ObjectId(directoryDTOResquest.getDirectory_parent_id()));
        return new DirectoryDTOResquest(directory.getName(), directory.getDirectory(), directory.getDirectoryParent().toHexString());
    }

    public void nameDirectoryCopy(Directory originalDirectory, String user_id) {
        String originalName = originalDirectory.getName();
        String newName = originalName;

        int copyCount = 1;
        Directory directoryp;

        do {
            directoryp = directoryRepository.findByNameAndUserAndDirectoryParentAndIsDeletedFalse(newName, new ObjectId(user_id), originalDirectory.getDirectoryParent());

            if (directoryp != null) {
                newName = originalName + "copy".repeat(copyCount);
                copyCount++;
            }
        } while (directoryp != null);

        originalDirectory.setName(newName);
    }

    public void moveDirectory(ObjectId originalDirectoryId, String user_id, ObjectId parentId){

            Optional<Directory> directory = directoryRepository.findById(parentId.toHexString());

        if (directory.isPresent()) {
            directoryRepository.newupdateByIdAndDirectoryParentAndUserDirectoryAndAndIsDeletedFalse(
                    originalDirectoryId, new ObjectId(user_id), parentId, directory.get().getDirectory() >= 0 ? directory.get().getDirectory() : 0
            );
        } else {
            directoryRepository.newupdateByIdAndDirectoryParentAndUserDirectoryAndAndIsDeletedFalse(originalDirectoryId,new ObjectId(user_id),parentId, 0);
        }
    }



    public void copyDirectory(ObjectId originalDirectoryId, ObjectId parentId, String user_id) throws FileNotFoundException {
        Directory originalDirectory = directoryRepository.findByIdAndIsDeletedFalse(originalDirectoryId);

        //Directory directoryp = directoryRepository.findByNameAndUserAndDirectoryParentAndIsDeletedFalse(originalDirectory.getName(),new ObjectId(user_id), originalDirectory.getDirectoryParent());

        if (count == 0){
            nameDirectoryCopy(originalDirectory, user_id);
        }


        count++;

        Directory newDirectory = Directory.builder()
                .name(originalDirectory.getName())
                .user(originalDirectory.getUser())
                .directory(originalDirectory.getDirectory())
                .directoryParent(parentId)
                .isDeleted(false)
                .created(new Date())
                .updated(new Date())
                .build();

        newDirectory = directoryRepository.save(newDirectory);

        List<File> filesInDirectory = fileRepository.findAllByUserIdAndDirectoryIdAndIsDeletedFalse(new ObjectId(user_id), originalDirectoryId);
        for (File originalFile : filesInDirectory) {

            GridFSFile gridFsFile = gridFsTemplate.findOne(
                    new Query(
                            Criteria.where("_id").is(originalFile.getGridFsFileId())
                    )
            );

            byte[] content = null;
            if (gridFsFile != null) {
                try (InputStream fileContent = gridFsTemplate.getResource(gridFsFile).getInputStream()) {
                    content = fileContent.readAllBytes();
                } catch (IOException e) {
                    throw new RuntimeException("Error al leer el contenido del archivo: " + originalFile.getName(), e);
                }

                ObjectId newGridFsFileId = gridFsTemplate.store(new ByteArrayInputStream(content), originalFile.getName(), originalFile.getFileType());

                File newFile = File.builder()
                        .name(originalFile.getName())
                        .directoryId(newDirectory.getId())
                        .userId(originalFile.getUserId())
                        .size(originalFile.getSize())
                        .fileType(originalFile.getFileType())
                        .gridFsFileId(newGridFsFileId)
                        .isDeleted(false)
                        .created(new Date())
                        .updated(new Date())
                        .build();
                fileRepository.save(newFile);
            } else {
                throw new FileNotFoundException("El recurso con ID " + originalFile.getGridFsFileId() + " no existe.");
            }
        }

        List<Directory> subdirectories = directoryRepository.findByDirectoryParentAndUserAndIsDeletedFalse(originalDirectoryId, new ObjectId(user_id));
        for (Directory subdirectory : subdirectories) {
            copyDirectory(subdirectory.getId(),newDirectory.getId(), user_id);
        }
    }

}
