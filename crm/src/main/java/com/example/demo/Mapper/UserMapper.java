package com.example.demo.Mapper;

import com.example.demo.DTO.CreateUserRequestDTO;
import com.example.demo.DTO.UserInfoDTO;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Model.User;

public interface UserMapper {

    UserInfoDTO toUserInfoDTO(User user);

    CreateUserRequestDTO toCreateUserRequestDTO(SignupRequest signupRequest);

}
