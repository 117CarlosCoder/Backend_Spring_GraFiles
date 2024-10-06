package com.archivos.api_grafiles_spring.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.Authentication;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    @Value("${security.jwt.key.private}")
    private String privatekey;

    @Value("${security.jwt.user.generator}")
    private String usergenerator;

    public String createToken(Authentication authentication, String userID){
        Algorithm alorithm = Algorithm.HMAC256(this.privatekey);
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
                .withExpiresAt(new Date(System.currentTimeMillis() + 1800000))
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis()))
                .sign(alorithm);
    };

    public DecodedJWT decodeToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(this.privatekey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(this.usergenerator)
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException e){
            throw new JWTVerificationException("Token no valido");
        }
    }

    public String extrectUsername(DecodedJWT decodedJWT){
        return decodedJWT.getSubject();
    }

    public Claim extractClaim(DecodedJWT decodedJWT, String claim){
        return decodedJWT.getClaim(claim);
    }

    public Map<String, Claim> extractAllClaims(DecodedJWT decodedJWt){
        return decodedJWt.getClaims();
    }
}
