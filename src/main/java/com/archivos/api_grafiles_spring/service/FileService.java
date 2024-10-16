package com.archivos.api_grafiles_spring.service;

import com.archivos.api_grafiles_spring.controller.dto.FileDTOResponse;
import com.archivos.api_grafiles_spring.persistence.model.DirectoryShared;
import com.archivos.api_grafiles_spring.persistence.model.File;
import com.archivos.api_grafiles_spring.persistence.model.UserModel;
import com.archivos.api_grafiles_spring.persistence.repository.DirectoryShareRepository;
import com.archivos.api_grafiles_spring.persistence.repository.FileRepository;
import com.archivos.api_grafiles_spring.persistence.repository.UserRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private DirectoryShareRepository directoryShareRepository;

    @Autowired
    private GridFsOperations gridFsOperations;

    private FileDTOResponse buildFileDTOResponse(File file) throws IOException {
        GridFSFile gridFsFile = gridFsTemplate.findOne(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("_id").is(file.getGridFsFileId())
                )
        );

        byte[] content = null;
        if (gridFsFile != null) {
            InputStream fileContent = gridFsOperations.getResource(gridFsFile).getInputStream();
            content = fileContent.readAllBytes();
        }

        return new FileDTOResponse(
                file.getId().toHexString(),
                file.getName(),
                file.getDirectoryId().toHexString(),
                file.getUserShare(),
                file.getSize(),
                file.getFileType(),
                file.getCreated(),
                file.getUpdated(),
                content
        );
    }

    public List<FileDTOResponse> getFilesWithContent(List<File> files) throws IOException {
        List<FileDTOResponse> fileDTOResponses = new ArrayList<>();

        for (File file : files) {
            fileDTOResponses.add(buildFileDTOResponse(file));
        }

        return fileDTOResponses;
    }


    private FileDTOResponse buildFileDTOResponse(DirectoryShared directoryShared) throws IOException {
        GridFSFile gridFsFile = gridFsTemplate.findOne(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("_id").is(directoryShared.getGridFsFileId())
                )
        );

        byte[] content = null;
        if (gridFsFile != null) {
            InputStream fileContent = gridFsTemplate.getResource(gridFsFile).getInputStream();
            content = fileContent.readAllBytes();
        }

        return new FileDTOResponse(
                directoryShared.getId().toHexString(),
                directoryShared.getName(),
                directoryShared.getDirectoryId().toHexString(),
                directoryShared.getUserShare(),
                directoryShared.getSize(),
                directoryShared.getFileType(),
                directoryShared.getCreated(),
                directoryShared.getUpdated(),
                content
        );
    }


    public List<FileDTOResponse> getSharedFilesWithContent(String userId) throws IOException {
        List<DirectoryShared> directoryShareds = directoryShareRepository.findAllByUserIdAndIsDeletedFalse(new ObjectId(userId));
        List<FileDTOResponse> fileDTOResponses = new ArrayList<>();

        for (DirectoryShared directoryShared : directoryShareds) {
            fileDTOResponses.add(buildFileDTOResponse(directoryShared));
        }

        return fileDTOResponses;
    }






    public File updateFile(MultipartFile file, String userId, ObjectId directoryId, ObjectId id) throws IOException {


        File filerep = fileRepository.findByNameAndUserIdAndDirectoryIdAndIsDeletedFalse(file.getOriginalFilename(),new ObjectId(userId),directoryId);

        if (!filerep.getId().equals(id)){
            return null;
        }
        else{

        InputStream inputStream = file.getInputStream();
        ObjectId gridFsFileId = gridFsTemplate.store(inputStream, file.getOriginalFilename(), file.getContentType());

        File newFile = File.builder()
                .id(id)
                .name(file.getOriginalFilename())
                .directoryId(directoryId)
                .userId(new ObjectId(userId))
                .size(file.getSize())
                .fileType(file.getContentType())
                .gridFsFileId(gridFsFileId)
                .isDeleted(false)
                .created(filerep.getCreated())
                .updated(new Date())
                .build();

        return fileRepository.save(newFile);
        }
    }

    public File saveFile(MultipartFile file, String userId, ObjectId directoryId) throws IOException {
        System.out.println(file.getOriginalFilename());
        System.out.println(file.getName());

        File filerep = fileRepository.findByNameAndUserIdAndDirectoryIdAndIsDeletedFalse(file.getOriginalFilename(),new ObjectId(userId),directoryId);

        if (filerep != null){
            return null;
        }
        else {
            InputStream inputStream = file.getInputStream();
            ObjectId gridFsFileId = gridFsTemplate.store(inputStream, file.getOriginalFilename(), file.getContentType());

            File newFile = File.builder()
                    .name(file.getOriginalFilename())
                    .directoryId(directoryId)
                    .userId(new ObjectId(userId))
                    .size(file.getSize())
                    .fileType(file.getContentType())
                    .gridFsFileId(gridFsFileId)
                    .isDeleted(false)
                    .created(new Date())
                    .updated(new Date())
                    .build();

            return fileRepository.save(newFile);
        }
    }

    public File getFileById(ObjectId fileId) {
        return fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));
    }

    public GridFSFile getFileFromGridFs(ObjectId fileId) {
        return gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));
    }

    public void deleteFile(ObjectId fileId,ObjectId user) {

        fileRepository.newDeletedByIdAndUser(fileId, user);


    }

    public void deleteFileShare(ObjectId fileId,ObjectId user) {

        directoryShareRepository.newDeletedByIdAndUser(fileId, user);
    }



    public void copyFile(ObjectId fileId, ObjectId userId) {

        File originalFile = fileRepository.findByIdAndIsDeletedFalse(fileId);

        if (originalFile == null) {
            throw new RuntimeException("Archivo no encontrado o ya eliminado.");
        }

        GridFSFile gridFsFile = gridFsTemplate.findOne(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("_id").is(originalFile.getGridFsFileId())
                )
        );

        if (gridFsFile == null) {
            throw new RuntimeException("El archivo no existe en GridFS.");
        }

        byte[] content;
        try (InputStream fileContent = gridFsTemplate.getResource(gridFsFile).getInputStream()) {
            content = fileContent.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el contenido del archivo: " + originalFile.getName(), e);
        }

        String originalFullName = originalFile.getName();
        String originalName = originalFullName.contains(".")
                ? originalFullName.substring(0, originalFullName.lastIndexOf('.'))
                : originalFullName;
        String extension = originalFullName.contains(".")
                ? originalFullName.substring(originalFullName.lastIndexOf('.'))
                : "";

        String newName = originalName;
        int copyCount = 1;
        File fileCopy;

        do {

            fileCopy = fileRepository.findByNameAndUserIdAndDirectoryIdAndIsDeletedFalse(newName + extension, userId, originalFile.getDirectoryId());

            if (fileCopy != null) {

                newName = originalName + "copy".repeat(copyCount);
                copyCount++;
            }
        } while (fileCopy != null);

        ObjectId newGridFsFileId = gridFsTemplate.store(new ByteArrayInputStream(content), newName + extension, originalFile.getFileType());

        File newFile = File.builder()
                .name(newName + extension)
                .directoryId(originalFile.getDirectoryId())
                .userId(userId)
                .size(originalFile.getSize())
                .fileType(originalFile.getFileType())
                .gridFsFileId(newGridFsFileId)
                .isDeleted(false)
                .created(new Date())
                .updated(new Date())
                .build();

        fileRepository.save(newFile);
    }


    public void moveFile(ObjectId originalDirectoryId, String user_id, ObjectId parentId){

        fileRepository.newupdateByIdAndDirectoryParentAndUserDirectoryAndAndIsDeletedFalse(originalDirectoryId,new ObjectId(user_id),parentId);
    }

    public void shareFile(ObjectId originalDirectoryId, String user_id, String email) {
        Optional<UserModel> userOptional = userRepository.findByEmail(email);
        Optional<UserModel> userOptional2 = userRepository.findById(user_id);
        if (userOptional.isPresent() && userOptional2.isPresent()) {
            UserModel user = userOptional.get();
            UserModel user2 = userOptional2.get();

            Optional<File> fileOptional = fileRepository.findById(originalDirectoryId);

            if (fileOptional.isPresent()) {
                File originalFile = fileOptional.get();

                ObjectId newGridFsFileId = copyFileShare(originalFile.getId(), user.getId()); // Asumimos que copyFile devuelve el nuevo ID del archivo

                DirectoryShared newDirectoryShared = DirectoryShared.builder()
                        .name(originalFile.getName())
                        .directoryId(new ObjectId("000000000000000000000000")) // O el ID correspondiente si es necesario
                        .userId(user.getId())
                        .userShare(user2.getEmail())
                        .size(originalFile.getSize())
                        .fileType(originalFile.getFileType())
                        .gridFsFileId(newGridFsFileId)
                        .isDeleted(false)
                        .created(new Date())
                        .updated(new Date())
                        .build();

                directoryShareRepository.save(newDirectoryShared);

                System.out.println("Archivo compartido con éxito.");
            } else {
                throw new RuntimeException("No se encontró el archivo original o está eliminado.");
            }
        } else {
            throw new RuntimeException("Usuario con el email proporcionado no encontrado.");
        }
    }

    private ObjectId copyFileShare(ObjectId fileId, ObjectId userId) {
        File originalFile = fileRepository.findByIdAndIsDeletedFalse(fileId);
        if (originalFile == null) {
            throw new RuntimeException("Archivo no encontrado o ya eliminado.");
        }

        GridFSFile gridFsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(originalFile.getGridFsFileId())));
        if (gridFsFile == null) {
            throw new RuntimeException("El archivo no existe en GridFS.");
        }

        byte[] content;
        try (InputStream fileContent = gridFsTemplate.getResource(gridFsFile).getInputStream()) {
            content = fileContent.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el contenido del archivo: " + originalFile.getName(), e);
        }

        String newName = originalFile.getName();
        ObjectId newGridFsFileId = gridFsTemplate.store(new ByteArrayInputStream(content), newName, originalFile.getFileType());

        

        return newGridFsFileId;
    }


}
