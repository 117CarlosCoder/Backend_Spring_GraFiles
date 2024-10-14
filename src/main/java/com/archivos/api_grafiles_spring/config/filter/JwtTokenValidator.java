package com.archivos.api_grafiles_spring.config.filter;

import com.archivos.api_grafiles_spring.util.JwtUtils;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.util.Collection;

public class JwtTokenValidator extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtTokenValidator(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String jwtToken = getTokenFromRequest(request);

        if (jwtToken != null) {
            try {

                DecodedJWT decodedJWT = jwtUtils.decodeToken(jwtToken, response);
                String username = jwtUtils.extractUsername(decodedJWT);
                String authorities = jwtUtils.extractClaim(decodedJWT, "authorities").asString();


                Collection<? extends GrantedAuthority> authoritiesList = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);


                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authoritiesList);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {

                System.out.println("Error al procesar el token JWT: " + e.getMessage());
            }
        }


        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {

        String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);


        if (jwtToken == null) {
            jwtToken = getTokenFromCookies(request);
        } else {

            jwtToken = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7) : jwtToken;
        }

        return jwtToken;
    }

    private String getTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwtToken".equals(cookie.getName())) {
                    return cookie.getValue(); 
                }
            }
        }
        return null;
    }
}
