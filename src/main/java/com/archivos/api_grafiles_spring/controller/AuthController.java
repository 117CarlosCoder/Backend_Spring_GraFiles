package com.archivos.api_grafiles_spring.controller;

import com.archivos.api_grafiles_spring.controller.dto.AuthLoginRequest;
import com.archivos.api_grafiles_spring.controller.dto.AuthResponse;
import com.archivos.api_grafiles_spring.persistence.model.UserModel;
import com.archivos.api_grafiles_spring.persistence.repository.UserRepository;
import com.archivos.api_grafiles_spring.service.UserDetailServiceImpl;
import jakarta.validation.Valid;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserDetailServiceImpl userDetailService;
    @Autowired
    private UserRepository userRepository;


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid AuthLoginRequest userRequest){
        try {
            AuthResponse authResponse = this.userDetailService.loginUser(userRequest);

            UserModel userEntity = userRepository.findUserEntityByUsername(userRequest.username())
                    .orElseThrow(() -> new UsernameNotFoundException("El usuario " + userRequest.username() + " no existe."));

            String userRole = userEntity.getRole().name();

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("role", userRole);
            responseBody.put("message", authResponse.message());
            responseBody.put("jwtToken", authResponse.jwtToken());
            responseBody.put("status", authResponse.status().toString());

            ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", authResponse.jwtToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(24 * 60 * 60)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(responseBody);
        }catch (UsernameNotFoundException ex) {
            // Devolver error si el usuario no se encuentra
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Usuario no encontrado");
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (BadCredentialsException ex) {
            // Devolver error si las credenciales son incorrectas
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Credenciales incorrectas");
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception ex) {
            // Devolver error genérico para otros casos
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error en el proceso de autenticación");
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping(value = "logout")
    public ResponseEntity<String> logout( Response response)
    {
        return this.userDetailService.logout(response);
    }
}