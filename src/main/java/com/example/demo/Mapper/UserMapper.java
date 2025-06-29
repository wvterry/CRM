package com.example.demo.Mapper;

import com.example.demo.DTO.*;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Model.User;

public interface UserMapper {

    public User toUserFromSignupRequest(SignupRequest signupRequest);

    public User toUserFromUpdateUserDTO(User user, UpdateUserDTO updateUserDTO);

    public UpdatedUserDTO toUpdatedUserDTOFromUser(User user);

    public User toUserFromUpdatePasswordDTO(User user, UpdatePasswordDTO updatePasswordDTO);

    public UserInfoDTO toUserInfoDTOFromUser(User user);

    public User toUserFromUpdateUserRoleDTO(User user, UpdateUserRoleDTO updateUserRoleDTO);
}
