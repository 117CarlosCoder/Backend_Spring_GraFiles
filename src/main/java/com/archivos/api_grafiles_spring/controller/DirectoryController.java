package com.archivos.api_grafiles_spring.controller;

import com.archivos.api_grafiles_spring.controller.dto.DirectoryCopiDTORequest;
import com.archivos.api_grafiles_spring.controller.dto.DirectoryDTOResponse;
import com.archivos.api_grafiles_spring.controller.dto.DirectoryDTOResquest;
import com.archivos.api_grafiles_spring.controller.dto.UpdateDirectoryDTORequest;
import com.archivos.api_grafiles_spring.service.DirectoryService;
import com.archivos.api_grafiles_spring.util.JwtUtils;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/directory")
public class DirectoryController {

    private final JwtUtils jwtUtils;

    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private HttpServletResponse httpServletResponse;

    public DirectoryController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/created")
    public ResponseEntity<String> directoryCreated(@RequestBody DirectoryDTOResquest directoryDTOResquest){
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        return directoryService.createDirectory(directoryDTOResquest, id_user);
    }

    @PutMapping("/updated")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> directoryUpdate(@RequestBody UpdateDirectoryDTORequest updateDirectoryDTORequest){
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        return directoryService.updateDirectory(updateDirectoryDTORequest, id_user);
    }

    @GetMapping("/gets")
    public ResponseEntity<List<DirectoryDTOResponse>> getDirectorys(@RequestParam String id){
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        return directoryService.getDirectorys(id, id_user);
    }

    @PostMapping("/get")
    @ResponseStatus(HttpStatus.OK)
    public DirectoryDTOResquest getDirectory(@RequestBody DirectoryDTOResquest directoryDTOResquest){
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        return directoryService.getDirectory(directoryDTOResquest, id_user);
    }

    @DeleteMapping("/deleted")
    @ResponseStatus(HttpStatus.OK)
    public String deleteDirectory(@RequestParam String id){
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        return directoryService.deleteDirectory(id, id_user);
    }

    @PostMapping("/copy")
    @ResponseStatus(HttpStatus.OK)
    public String copyDirectory(@RequestBody DirectoryCopiDTORequest directoryCopiDTORequest){
        System.out.println("------------------------------------------------");
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        try {
            directoryService.copyDirectory(new ObjectId(directoryCopiDTORequest.getId()),new ObjectId(directoryCopiDTORequest.getDirectory_parent_id()), id_user);
            directoryService.count = 0;
            System.out.println("Copiar");
            return "Copiado";
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/move")
    @ResponseStatus(HttpStatus.OK)
    public String moveDirectory(@RequestBody DirectoryCopiDTORequest directoryCopiDTORequest){
        System.out.println("------------------------------------------------");
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        System.out.println("mover");
        try {
            directoryService.moveDirectory(new ObjectId(directoryCopiDTORequest.getId()), id_user,new ObjectId(directoryCopiDTORequest.getDirectory_parent_id()));
            return "Mover";
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
