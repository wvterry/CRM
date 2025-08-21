package com.example.userservice.Mapper;

import com.example.userservice.DTO.*;
import com.example.userservice.Model.User;



public interface UserMapper {

    UserInfoDTO toUserInfoDTO(User user);

    User toUser(CreateUserDTO createUserDTO);

    UserIdResponseDTO toUserIdResponseDTO(User user);

}
