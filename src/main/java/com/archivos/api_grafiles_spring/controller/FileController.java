package com.archivos.api_grafiles_spring.controller;

import com.archivos.api_grafiles_spring.controller.dto.DirectoryCopiDTORequest;
import com.archivos.api_grafiles_spring.controller.dto.FileDTOResponse;
import com.archivos.api_grafiles_spring.persistence.model.DirectoryShared;
import com.archivos.api_grafiles_spring.persistence.model.File;
import com.archivos.api_grafiles_spring.persistence.repository.DirectoryShareRepository;
import com.archivos.api_grafiles_spring.persistence.repository.FileRepository;
import com.archivos.api_grafiles_spring.service.FileService;
import com.archivos.api_grafiles_spring.util.JwtUtils;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.springframework.boot.logging.logback.RollingPolicySystemProperty.FILE_NAME_PATTERN;

@RestController
@RequestMapping("/file")
public class FileController {

    private final JwtUtils jwtUtils;

    private static final String FILE_NAME_REGEX = "^[a-zA-Z0-9-_]+$";

    @Autowired
    private FileService fileService;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private HttpServletResponse httpServletResponse;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private DirectoryShareRepository directoryShareRepository;

    public FileController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/gets")
    public ResponseEntity<List<FileDTOResponse>> getFiles(@RequestParam ObjectId id) {
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        try {
            List<File> files = List.of();

            if (id != null && id_user != null) {
                files = fileRepository.findAllByUserIdAndDirectoryIdAndIsDeletedFalse(new ObjectId(id_user), id);
            }

            List<FileDTOResponse> fileDTOResponses = fileService.getFilesWithContent(files);

            return ResponseEntity.ok(fileDTOResponses);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/get/shared")
    public ResponseEntity<List<FileDTOResponse>> getFilesShared() throws IOException {
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);


        List<FileDTOResponse> fileDTOResponses = fileService.getSharedFilesWithContent(id_user);
        System.out.println("Sharedfiles " + fileDTOResponses);
        return ResponseEntity.ok(fileDTOResponses);
    }

    @PutMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("directoryId") String directoryId,
                                             @RequestParam("id") String id) {
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        try {
            File savedFile = fileService.updateFile(file, id_user, new ObjectId(directoryId), new ObjectId(id));
            return ResponseEntity.ok("File uploaded successfully with ID: " + savedFile.getId());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/created")
    public ResponseEntity<String> createdFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("directoryId") String directoryId) {

        String originalFilename = file.getOriginalFilename();

        String fileName = originalFilename != null ? originalFilename.substring(0, originalFilename.lastIndexOf(".")) : "";

        if (!fileName.matches(FILE_NAME_REGEX)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El nombre del archivo solo puede contener letras, números, guiones (-) y guiones bajos (_).");
        }

        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        try {
            File savedFile = fileService.saveFile(file, id_user, new ObjectId(directoryId));
            return ResponseEntity.ok("File uploaded successfully with ID: " + savedFile.getId());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }


    @PostMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam ObjectId fileId) throws IOException {
        File file = fileService.getFileById(fileId);  // Obtener archivo de la colección 'files'
        GridFSFile gridFsFile = fileService.getFileFromGridFs(file.getGridFsFileId());  // Obtener archivo de GridFS

        if (gridFsFile == null) {
            return ResponseEntity.status(404).body(null);
        }

        GridFsResource resource = gridFsTemplate.getResource(gridFsFile);
        InputStream inputStream = resource.getInputStream();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(new InputStreamResource(inputStream));
    }

    @DeleteMapping("/deleted")
    public ResponseEntity<String> deleteFile(@RequestParam ObjectId id) {
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        try {
            fileService.deleteFile(id, new ObjectId(id_user));
            return ResponseEntity.ok("File deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("File deletion failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/deletedShare")
    public ResponseEntity<String> deleteFileShare(@RequestParam ObjectId id) {
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        try {
            fileService.deleteFileShare(id, new ObjectId(id_user));
            return ResponseEntity.ok("File deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("File deletion failed: " + e.getMessage());
        }
    }

    @PostMapping("/copy")
    public ResponseEntity<String> copyFile(@RequestParam ObjectId id) {
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        try {
            fileService.copyFile(id, new ObjectId(id_user));
            return ResponseEntity.ok("File deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("File deletion failed: " + e.getMessage());
        }
    }

    @PostMapping("/move")
    public ResponseEntity<String> moveFile(@RequestBody DirectoryCopiDTORequest directoryCopiDTORequest) {
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        try {
            fileService.moveFile(new ObjectId(directoryCopiDTORequest.getId()), id_user,new ObjectId(directoryCopiDTORequest.getDirectory_parent_id()));
            return ResponseEntity.ok("File move successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("File move failed: " + e.getMessage());
        }
    }

    @PostMapping("/share")
    @ResponseStatus(HttpStatus.OK)
    public String shareDirectory(@RequestParam String id , @RequestParam String email){
        System.out.println("------------------------------------------------");
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        System.out.println("compartir");
        try {
            fileService.shareDirectory(new ObjectId(id),id_user,email);
            return "Compartir";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        System.out.println("cokkies " + Arrays.toString(cookies));
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    System.out.printf("jwtToken"+ cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String extractUserIDFromToken(String token) {
        try {
            DecodedJWT decodedJWT = jwtUtils.decodeToken(token, httpServletResponse);
            Map<String, Claim> claims = decodedJWT.getClaims();
            claims.forEach((key, value) -> System.out.println(key + ": " + value.asString()));
            return decodedJWT.getClaim("id_user").asString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract id_user from token", e);
        }
    }

}
