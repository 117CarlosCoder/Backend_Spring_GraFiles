package com.archivos.api_grafiles_spring.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    @Value("${security.jwt.key.private}")
    private String privatekey;

    @Value("${security.jwt.user.generator}")
    private String usergenerator;

    // La lista negra para tokens revocados
    private Set<String> tokenBlacklist = new HashSet<>();

    // Método para crear el token
    public String createToken(Authentication authentication, String userID) {
        Algorithm algorithm = Algorithm.HMAC256(this.privatekey);
        String username = authentication.getPrincipal().toString();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return JWT.create()
                .withIssuer(this.usergenerator)
                .withSubject(username)
                .withClaim("authorities", authorities)
                .withClaim("id_user", userID)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1800000)) // 30 minutos
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis()))
                .sign(algorithm);
    }

    // Método para decodificar el token
    public DecodedJWT decodeToken(String token, HttpServletResponse response) {
        if (isTokenBlacklisted(token)) {
            invalidateToken(response); // Llamar al método para eliminar la cookie si el token está en la lista negra
            throw new JWTVerificationException("Token inválido o revocado.");
        }

        try {
            Algorithm algorithm = Algorithm.HMAC256(this.privatekey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(this.usergenerator)
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            invalidateToken(response);
            throw new JWTVerificationException("Token no válido.");
        }
    }

    // Método para extraer el nombre de usuario
    public String extractUsername(DecodedJWT decodedJWT) {
        return decodedJWT.getSubject();
    }

    // Método para extraer un claim específico
    public Claim extractClaim(DecodedJWT decodedJWT, String claim) {
        return decodedJWT.getClaim(claim);
    }

    // Método para extraer todos los claims
    public Map<String, Claim> extractAllClaims(DecodedJWT decodedJWT) {
        return decodedJWT.getClaims();
    }

    // Método para invalidar el token y eliminar la cookie
    public void invalidateToken(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwtToken", "");
        cookie.setPath("/"); // Establecer el path correcto
        cookie.setMaxAge(0); // Establecer a 0 para eliminar la cookie
        cookie.setHttpOnly(true); // Asegurar que no sea accesible desde JavaScript
        cookie.setSecure(true); // Asegurarse de que solo se envíe por HTTPS (opcional)
        response.addCookie(cookie); // Agregar la cookie a la respuesta
    }

    // Método para verificar si el token está en la lista negra
    private boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }

    // Método para revocar un token (agregar a la lista negra)
    public void blacklistToken(String token) {
        tokenBlacklist.add(token);
    }
}
