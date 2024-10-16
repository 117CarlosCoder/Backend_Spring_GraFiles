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

    private Set<String> tokenBlacklist = new HashSet<>();

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

    public DecodedJWT decodeToken(String token, HttpServletResponse response) {
        if (isTokenBlacklisted(token)) {
            invalidateToken(response);
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


    public String extractUsername(DecodedJWT decodedJWT) {
        return decodedJWT.getSubject();
    }

    public Claim extractClaim(DecodedJWT decodedJWT, String claim) {
        return decodedJWT.getClaim(claim);
    }

    public void invalidateToken(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwtToken", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
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
