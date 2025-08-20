package com.example.demo.Mapper;

import com.example.demo.DTO.*;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Model.Role;
import com.example.demo.Model.User;

import java.util.Set;

public interface UserMapper {

    public UserInfoDTO toUserInfoDTO(User user);

    public CreateUserRequestDTO toCreateUserRequestDTO(SignupRequest signupRequest);

}
