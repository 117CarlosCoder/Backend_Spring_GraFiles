package com.archivos.api_grafiles_spring.controller;

import com.archivos.api_grafiles_spring.controller.dto.AuthResponse;
import com.archivos.api_grafiles_spring.controller.dto.CreateUserRequest;
import com.archivos.api_grafiles_spring.controller.dto.DirectoryDTOResponse;
import com.archivos.api_grafiles_spring.controller.dto.FileDTOResponse;
import com.archivos.api_grafiles_spring.persistence.model.File;
import com.archivos.api_grafiles_spring.persistence.repository.FileRepository;
import com.archivos.api_grafiles_spring.service.AdminService;
import com.archivos.api_grafiles_spring.service.UserDetailServiceImpl;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @GetMapping("/getDirectorys")
    public List<DirectoryDTOResponse> getDirectorys(@RequestParam String id){
        return adminService.getDirectorys(id);
    }

    @GetMapping("/getFiles")
    public ResponseEntity<List<FileDTOResponse>>  getFiles(@RequestParam String id){
        try {
            List<File> files = List.of();

            if (id != null ) {
                files = fileRepository.findAllByDirectoryIdAndIsDeletedTrue(new ObjectId(id));
            }

            List<FileDTOResponse> fileDTOResponses = adminService.getFilesWithContent(files);

            return ResponseEntity.ok(fileDTOResponses);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/getSharedFiles")
    public ResponseEntity<List<FileDTOResponse>>  getShareFiles(@RequestParam String id){
        try {

            List<FileDTOResponse> fileDTOResponses = adminService.getSharedFilesWithContent(id);

            return ResponseEntity.ok(fileDTOResponses);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/create/User")
    public ResponseEntity<AuthResponse> createUser(@RequestBody CreateUserRequest createUserRequest){
        return adminService.createUser(createUserRequest);
    }
}
