package com.archivos.api_grafiles_spring.service;

import com.archivos.api_grafiles_spring.controller.dto.ChangePasswordDTORequest;
import com.archivos.api_grafiles_spring.controller.dto.UserInfoResponse;
import com.archivos.api_grafiles_spring.controller.dto.UserPasswordDTO;
import com.archivos.api_grafiles_spring.persistence.model.UserModel;
import com.archivos.api_grafiles_spring.persistence.repository.UserInfoRepository;
import com.archivos.api_grafiles_spring.persistence.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<UserInfoResponse> getInfoUser(String id){
        Optional<UserInfoResponse> userInfoResponse;
        try {
            Optional<UserModel> userModel = userRepository.findById(id);
            userInfoResponse = Optional.of(new UserInfoResponse(userModel.get().getName(), userModel.get().getEmail()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().body(userInfoResponse.orElse(null));
    }

    public ResponseEntity<String> changePassword(String id_user, ChangePasswordDTORequest changePasswordDTORequest){

        try {
            Optional<UserPasswordDTO> userModel = userRepository.findById(new ObjectId(id_user));
            System.out.println(userModel);

            String password ="";
            if (passwordEncoder.matches( changePasswordDTORequest.actualPassword(),userModel.get().getPassword())){
                System.out.println("Haciendo cambio");
                password= passwordEncoder.encode(changePasswordDTORequest.newPassword());
                userRepository.newPassword(new ObjectId(id_user),password);

            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La constraseña no es la actual");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().body("Cambio de constraseña completado con exito");
    }



}
