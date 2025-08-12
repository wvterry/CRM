package com.example.userservice.Mapper;

import com.example.userservice.DTO.CreateUserDTO;
import com.example.userservice.DTO.UpdateUserDTO;
import com.example.userservice.DTO.UserIdResponseDTO;
import com.example.userservice.DTO.UserInfoDTO;
import com.example.userservice.Model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImp implements UserMapper{

    @Override
    public UserInfoDTO toUserInfoDTO(User user) {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setFirstName(user.getFirstName());
        userInfoDTO.setLastName(user.getLastName());
        return userInfoDTO;
    }

    @Override
    public User toUser(CreateUserDTO createUserDTO) {
        User user = new User();
        user.setFirstName(createUserDTO.getFirstName());
        user.setLastName(createUserDTO.getLastName());
        return user;
    }

    @Override
    public UserIdResponseDTO toUserIdResponseDTO(User user) {
        UserIdResponseDTO userIdResponseDTO = new UserIdResponseDTO();
        userIdResponseDTO.setUserId(user.getUserId());
        return userIdResponseDTO;
    }

    @Override
    public User toUser(User user, UpdateUserDTO updateUserDTO) {
        user.setFirstName(updateUserDTO.getFirstName());
        user.setLastName(updateUserDTO.getLastName());
        return user;
    }
}
