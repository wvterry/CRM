package com.example.demo.Mapper;

import com.example.demo.DTO.*;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Model.User;
import com.example.demo.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.example.demo.Model.Role;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class UserMapperImp implements UserMapper{

//    PasswordEncoder passwordEncoder;
//
//    private final RoleRepository roleRepository;

    @Autowired
    public UserMapperImp(PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
//        this.passwordEncoder = passwordEncoder;
//        this.roleRepository = roleRepository;
    }

    @Override
    public User toUser(Set<Role> roles, String password, SignupRequest signupRequest) {
        User user = new User();
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(password);
        user.setRoles(roles);
        return user;
    }

    @Override
    public User toUser(String password, User user, UpdateUserDTO updateUserDTO) {
        User updatedUser = new User();
        updatedUser.setUserId(user.getUserId());
        updatedUser.setFirstName(updateUserDTO.getFirstName());
        updatedUser.setLastName(updateUserDTO.getLastName());
        updatedUser.setEmail(updateUserDTO.getEmail());
        updatedUser.setPassword(password);
        updatedUser.setRoles(new HashSet<>(user.getRoles()));
        return updatedUser;
    }

    @Override
    public User toUser(String password, User user, UpdatePasswordDTO updatePasswordDTO) {
        User updatedUser = new User();
        updatedUser.setUserId(user.getUserId());
        updatedUser.setFirstName(user.getFirstName());
        updatedUser.setLastName(user.getLastName());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setRoles(new HashSet<>(user.getRoles()));
        updatedUser.setPassword(password);
        return updatedUser;
    }

    @Override
    public UserInfoDTO toUserInfoDTO(User user) {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setFirstName(user.getFirstName());
        userInfoDTO.setLastName(user.getLastName());
        userInfoDTO.setEmail(user.getEmail());
        return userInfoDTO;
    }

}
