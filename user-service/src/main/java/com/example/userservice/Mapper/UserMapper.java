package com.example.userservice.Mapper;

import com.example.userservice.DTO.CreateUserDTO;
import com.example.userservice.DTO.UserIdResponseDTO;
import com.example.userservice.DTO.UserInfoDTO;
import com.example.userservice.Model.User;


public interface UserMapper {
    UserInfoDTO toUserInfoDTO(User user);
    User toUser(CreateUserDTO createUserDTO);
    UserIdResponseDTO toUserIdResponseDTO(User user);
}
