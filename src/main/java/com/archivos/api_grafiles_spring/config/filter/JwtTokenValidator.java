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
        String jwtToken = getTokenFromRequest(request); // Obtener el token

        if (jwtToken != null) {
            try {
                // Decodificar el token y establecer la autenticación
                DecodedJWT decodedJWT = jwtUtils.decodeToken(jwtToken, response); // Pasar response aquí
                String username = jwtUtils.extractUsername(decodedJWT);
                String authorities = jwtUtils.extractClaim(decodedJWT, "authorities").asString();

                // Convertir las autoridades en una colección
                Collection<? extends GrantedAuthority> authoritiesList = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

                // Establecer la autenticación en el contexto de seguridad
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authoritiesList);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // Manejar cualquier error durante la verificación del token
                System.out.println("Error al procesar el token JWT: " + e.getMessage());
            }
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        // Intentar obtener el token del encabezado Authorization
        String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Si no se encuentra, buscar en las cookies
        if (jwtToken == null) {
            jwtToken = getTokenFromCookies(request);
        } else {
            // Extraer el token de tipo Bearer
            jwtToken = jwtToken.startsWith("Bearer ") ? jwtToken.substring(7) : jwtToken;
        }

        return jwtToken; // Devuelve el token o null
    }

    private String getTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwtToken".equals(cookie.getName())) {
                    return cookie.getValue(); // Devuelve el valor del token de la cookie
                }
            }
        }
        return null; // Devuelve null si no se encuentra el token
    }
}
