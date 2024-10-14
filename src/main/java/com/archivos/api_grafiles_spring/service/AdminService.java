package com.archivos.api_grafiles_spring.service;

import com.archivos.api_grafiles_spring.controller.dto.AuthResponse;
import com.archivos.api_grafiles_spring.controller.dto.CreateUserRequest;
import com.archivos.api_grafiles_spring.controller.dto.DirectoryDTOResponse;
import com.archivos.api_grafiles_spring.controller.dto.FileDTOResponse;
import com.archivos.api_grafiles_spring.persistence.model.Directory;
import com.archivos.api_grafiles_spring.persistence.model.DirectoryShared;
import com.archivos.api_grafiles_spring.persistence.model.File;
import com.archivos.api_grafiles_spring.persistence.repository.DirectoryRepository;
import com.archivos.api_grafiles_spring.persistence.repository.DirectoryShareRepository;
import com.archivos.api_grafiles_spring.persistence.repository.FileRepository;
import com.archivos.api_grafiles_spring.persistence.repository.UserRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private GridFsOperations gridFsOperations;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private DirectoryShareRepository directoryShareRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Autowired
    private FileRepository fileRepository;

    public List<DirectoryDTOResponse> getDirectorys(String id) {
        List<Directory> directories =  directoryRepository.findAllByDirectoryParentAndIsDeletedTrue(new ObjectId(id));
        return directories.stream().map(directory -> new DirectoryDTOResponse(
                directory.getId().toHexString(),
                directory.getName(),
                directory.getDirectory(),
                directory.getDirectoryParent().toHexString(),
                directory.getCreated(),
                directory.getUpdated()
        )).collect(Collectors.toList());
    }

    public List<FileDTOResponse> getFilesWithContent(List<File> files) throws IOException {
        List<FileDTOResponse> fileDTOResponses = new ArrayList<>();

        for (File file : files) {
            fileDTOResponses.add(buildFileDTOResponse(file));
        }

        return fileDTOResponses;
    }

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

    public List<FileDTOResponse> getSharedFilesWithContent(String id) throws IOException {
        List<DirectoryShared> directoryShareds = directoryShareRepository.findAllByDirectoryIdAndIsDeletedTrue(new ObjectId(id));
        List<FileDTOResponse> fileDTOResponses = new ArrayList<>();

        for (DirectoryShared directoryShared : directoryShareds) {
            fileDTOResponses.add(buildFileDTOResponse(directoryShared));
        }

        return fileDTOResponses;
    }

    public ResponseEntity<AuthResponse> createUser(CreateUserRequest createUserRequest){
        return new ResponseEntity<>(this.userDetailService.createUser(createUserRequest), HttpStatus.CREATED);
    }

}
