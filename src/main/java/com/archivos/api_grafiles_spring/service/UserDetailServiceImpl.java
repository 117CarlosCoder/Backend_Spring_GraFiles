package com.archivos.api_grafiles_spring.service;

import com.archivos.api_grafiles_spring.controller.dto.AuthLoginRequest;
import com.archivos.api_grafiles_spring.controller.dto.AuthResponse;
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

        // Si el usuario tiene un rol de ADMIN, podrías agregar más permisos
        if (userEntity.getRole() == RoleEnum.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("PERMISO_ADMIN"));
        }

        return new User(userEntity.getUsername(), userEntity.getPassword(), userEntity.isEnabled(),
                userEntity.isAccountNoExpired(), userEntity.isCredentialNoExpired(), userEntity.isAccountNoLocked(),
                authorities);
    }

    /*public AuthResponse createUser(AuthCreateUserRequest createRoleRequest) {

        String username = createRoleRequest.username();
        String password = createRoleRequest.password();
        List<String> rolesRequest = createRoleRequest.roleRequest().roleListName();

        Set<RoleEntity> roleEntityList = new HashSet<>(roleRepository.findRoleEntitiesByRoleEnumIn(rolesRequest));

        if (roleEntityList.isEmpty()) {
            throw new IllegalArgumentException("The roles specified does not exist.");
        }

        UserEntity userEntity = UserEntity.builder().username(username).password(passwordEncoder.encode(password)).roles(roleEntityList).isEnabled(true).accountNoLocked(true).accountNoExpired(true).credentialNoExpired(true).build();

        UserEntity userSaved = userRepository.save(userEntity);

        ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();

        userSaved.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_".concat(role.getRoleEnum().name()))));

        userSaved.getRoles().stream().flatMap(role -> role.getPermissionList().stream()).forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getName())));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userSaved, null, authorities);

        String accessToken = jwtUtils.createToken(authentication, createRoleRequest.Sucursal(), userEntity.getId().toString());

        return new AuthResponse( "User created successfully", accessToken, true,null);
    }*/

    public AuthResponse loginUser(AuthLoginRequest authLoginRequest) {

        String username = authLoginRequest.username();
        String password = authLoginRequest.password();

        UserModel userEntity = userRepository.findUserEntityByUsername(authLoginRequest.username())
                .orElseThrow(() -> new UsernameNotFoundException("El usuario " + authLoginRequest.username() + " no existe."));



        Authentication authentication = this.authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.createToken(authentication, userEntity.getId().toString());
        return new AuthResponse("User loged succesfully", accessToken, true, null);
    }

    public ResponseEntity<String> logout(Response response) {
        ResponseCookie cookie1 = ResponseCookie.from("jwtToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();



        // Agregar ambas cookies a la cabecera "Set-Cookie" de la respuesta
        response.addHeader("Set-Cookie", cookie1.toString());

        return ResponseEntity.ok("Logged out successfully");
    }
    public Authentication authenticate(String username, String password) {
        UserDetails userDetails = this.loadUserByUsername(username);

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Incorrect Password");
        }

        return new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities());
    }
}
