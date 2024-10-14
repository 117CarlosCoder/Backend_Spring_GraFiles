package com.archivos.api_grafiles_spring.controller;

import com.archivos.api_grafiles_spring.controller.dto.ChangePasswordDTORequest;
import com.archivos.api_grafiles_spring.controller.dto.UserInfoResponse;
import com.archivos.api_grafiles_spring.service.UserService;
import com.archivos.api_grafiles_spring.util.JwtUtils;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/user")

public class UserController {

    private final JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private HttpServletResponse httpServletResponse;

    public UserController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/get/info")
    public ResponseEntity<UserInfoResponse> getEmployee() {
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        return userService.getInfoUser(id_user);
    }

    @PostMapping("/change/password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDTORequest changePasswordDTORequest) {
        String jwtToken = extractJwtFromCookie(httpServletRequest);
        System.out.println("Token " + jwtToken);
        String id_user = extractUserIDFromToken(jwtToken);
        System.out.println("id_user " + id_user);
        return userService.changePassword(id_user, changePasswordDTORequest);
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

    public Iterable<String> convertirIterable(String texto) {
        return new Iterable<>() {
            private int index = 0;

            @Override
            public Iterator<String> iterator() {
                return new Iterator<>() {

                    @Override
                    public boolean hasNext() {
                        return index < texto.length();
                    }

                    @Override
                    public String next() {
                        return String.valueOf(texto.charAt(index++));  // Convertir cada char a String
                    }
                };
            }
        };

    }
}
