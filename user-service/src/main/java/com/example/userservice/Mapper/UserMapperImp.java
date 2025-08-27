package com.example.userservice.Mapper;

import com.example.userservice.DTO.CreateUserDTO;
import com.example.userservice.DTO.UserIdResponseDTO;
import com.example.userservice.DTO.UserInfoDTO;
import com.example.userservice.Model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImp implements UserMapper {

    @Override
    public UserInfoDTO toUserInfoDTO(User user) {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setFirstName(user.getFirstName());
        userInfoDTO.setLastName(user.getLastName());
        return userInfoDTO;
    }

    @Override
    public User toUser(CreateUserDTO user) {
        return User.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    @Override
    public UserIdResponseDTO toUserIdResponseDTO(User user) {
        UserIdResponseDTO userIdResponseDTO = new UserIdResponseDTO();
        userIdResponseDTO.setUserId(user.getUserId());
        return userIdResponseDTO;
    }

}
