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

}
