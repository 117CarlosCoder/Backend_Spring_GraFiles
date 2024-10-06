package com.archivos.api_grafiles_spring.service;

import com.archivos.api_grafiles_spring.controller.dto.UserInfoResponse;
import com.archivos.api_grafiles_spring.persistence.model.UserModel;
import com.archivos.api_grafiles_spring.persistence.repository.UserInfoRepository;
import com.archivos.api_grafiles_spring.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    UserRepository userRepository;

    public UserInfoResponse getInfoUser(String id){
        Optional<UserInfoResponse> userInfoResponse;
        try {
            Optional<UserModel> userModel = userRepository.findById(id);
            userInfoResponse = Optional.of(new UserInfoResponse(userModel.get().getName(), userModel.get().getEmail()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return userInfoResponse.orElse(null);
    }

}
