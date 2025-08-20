package com.example.userservice.Mapper;

import com.example.userservice.DTO.*;
import com.example.userservice.Model.User;



public interface UserMapper {

    public UserInfoDTO toUserInfoDTO(User user);

    public User toUser(CreateUserDTO createUserDTO);

    public UserIdResponseDTO toUserIdResponseDTO(User user);

}
