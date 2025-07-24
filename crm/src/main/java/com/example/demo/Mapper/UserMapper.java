package com.example.demo.Mapper;

import com.example.demo.DTO.*;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Model.Role;
import com.example.demo.Model.User;

import java.util.Set;

public interface UserMapper {

    public User toUser(Set<Role> roles, String password, SignupRequest signupRequest);

    public User toUser(String password, User user, UpdateUserDTO updateUserDTO);

    public User toUser(String password, User user, UpdatePasswordDTO updatePasswordDTO);

    public UserInfoDTO toUserInfoDTO(User user);

}
