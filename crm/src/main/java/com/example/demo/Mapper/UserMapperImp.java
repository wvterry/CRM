package com.example.demo.Mapper;

import com.example.demo.DTO.CreateUserRequestDTO;
import com.example.demo.DTO.UpdatePasswordDTO;
import com.example.demo.DTO.UpdateUserDTO;
import com.example.demo.DTO.UserInfoDTO;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Model.Role;
import com.example.demo.Model.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserMapperImp implements UserMapper{

    @Override
    public User toUser(User user, UpdateUserDTO updateUserDTO) {
        User updatedUser = new User();
        updatedUser.setUserId(user.getUserId());
        updatedUser.setFirstName(updateUserDTO.getFirstName());
        updatedUser.setLastName(updateUserDTO.getLastName());
        return updatedUser;
    }

    @Override
    public UserInfoDTO toUserInfoDTO(User user) {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setFirstName(user.getFirstName());
        userInfoDTO.setLastName(user.getLastName());
        return userInfoDTO;
    }

    @Override
    public CreateUserRequestDTO toCreateUserRequestDTO(SignupRequest signupRequest) {
        CreateUserRequestDTO createUserRequestDTO = new CreateUserRequestDTO();
        createUserRequestDTO.setFirstName(signupRequest.getFirstName());
        createUserRequestDTO.setLastName(signupRequest.getLastName());
        return createUserRequestDTO;
    }

    @Override
    public User toUser(Long userId, SignupRequest signupRequest) {
        User user = new User();
        user.setUserId(userId);
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        return user;
    }
}
