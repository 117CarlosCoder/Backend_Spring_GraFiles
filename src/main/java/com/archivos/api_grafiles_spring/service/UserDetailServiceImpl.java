package com.archivos.api_grafiles_spring.service;

import com.archivos.api_grafiles_spring.controller.dto.AuthLoginRequest;
import com.archivos.api_grafiles_spring.controller.dto.AuthResponse;
import com.archivos.api_grafiles_spring.controller.dto.CreateUserRequest;
import com.archivos.api_grafiles_spring.persistence.model.RoleEnum;
import com.archivos.api_grafiles_spring.persistence.model.UserModel;
import com.archivos.api_grafiles_spring.persistence.repository.UserRepository;
import com.archivos.api_grafiles_spring.util.JwtUtils;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) {

        UserModel userEntity = userRepository.findUserEntityByUsername(username).orElseThrow(() -> new UsernameNotFoundException("El usuario " + username + " no existe."));

        if (!userEntity.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario está deshabilitado.");
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_".concat(userEntity.getRole().name()));

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(authority);

        System.out.println(username);
        System.out.println("Rol del usuario: " + authority.getAuthority());

        if (userEntity.getRole() == RoleEnum.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("PERMISO_ADMIN"));
        }

        return new User(userEntity.getUsername(), userEntity.getPassword(), userEntity.isEnabled(),
                userEntity.isAccountNoExpired(), userEntity.isCredentialNoExpired(), userEntity.isAccountNoLocked(),
                authorities);
    }

    public AuthResponse createUser(CreateUserRequest createRoleRequest) {

        String username = createRoleRequest.username();
        String password = createRoleRequest.password();

        UserModel userEntity = UserModel.builder()
                .name(createRoleRequest.name())
                .username(username)
                .email(createRoleRequest.email())
                .password(passwordEncoder.encode(password))
                .role(RoleEnum.EMPLOYEE)
                .isEnabled(true)
                .accountNoLocked(true)
                .accountNoExpired(true)
                .credentialNoExpired(true)
                .build();

        UserModel userSaved = userRepository.save(userEntity);

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_".concat(userSaved.getRole().name())));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userSaved, null, authorities);
        String accessToken = jwtUtils.createToken(authentication, userEntity.getId().toString());

        return new AuthResponse("Usuario Creado con exito", accessToken, true, null);
    }



    public AuthResponse loginUser(AuthLoginRequest authLoginRequest) {

        String username = authLoginRequest.username();
        String password = authLoginRequest.password();

        UserModel userEntity = userRepository.findUserEntityByUsername(authLoginRequest.username())
                .orElseThrow(() -> new UsernameNotFoundException("El usuario " + authLoginRequest.username() + " no existe."));



        Authentication authentication = this.authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.createToken(authentication, userEntity.getId().toString());
        return new AuthResponse("Inicion de Sesion Exitoso", accessToken, true, null);
    }

    public ResponseEntity<String> logout(Response response) {
        ResponseCookie cookie1 = ResponseCookie.from("jwtToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie1.toString());

        return ResponseEntity.ok("Cierre de sesion exitoso");
    }
    public Authentication authenticate(String username, String password) {
        UserDetails userDetails = this.loadUserByUsername(username);

        if (userDetails == null) {
            throw new BadCredentialsException("Usuario Incorrecto");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Contraseña Incorrecta");
        }

        return new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities());
    }
}
