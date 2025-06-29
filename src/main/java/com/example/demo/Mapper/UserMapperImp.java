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

    PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    @Autowired
    public UserMapperImp(PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public User toUserFromSignupRequest(SignupRequest signupRequest) {
        User user = new User();
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        Set<Role> roles = new HashSet<>();
        Optional<Role> userRole = roleRepository.findByName("USER");
        roles.add(userRole.get());

        user.setRoles(roles);
        return user;
    }

    @Override
    public User toUserFromUpdateUserDTO(User user, UpdateUserDTO updateUserDTO) {
        User updatedUser = new User();
        updatedUser.setUserId(user.getUserId());
        updatedUser.setFirstName(updateUserDTO.getFirstName());
        updatedUser.setLastName(updateUserDTO.getLastName());
        updatedUser.setEmail(updateUserDTO.getEmail());
        updatedUser.setPassword(passwordEncoder.encode(user.getPassword()));
        updatedUser.setRoles(new HashSet<>(user.getRoles()));
        return updatedUser;
    }

    @Override
    public UpdatedUserDTO toUpdatedUserDTOFromUser(User user) {
        UpdatedUserDTO updatedUserDTO = new UpdatedUserDTO();
        updatedUserDTO.setFirstName(user.getFirstName());
        updatedUserDTO.setLastName(user.getLastName());
        updatedUserDTO.setEmail(user.getEmail());
        return updatedUserDTO;
    }

    @Override
    public User toUserFromUpdatePasswordDTO(User user, UpdatePasswordDTO updatePasswordDTO) {
        User updatedUser = new User();
        updatedUser.setUserId(user.getUserId());
        updatedUser.setFirstName(user.getFirstName());
        updatedUser.setLastName(user.getLastName());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setRoles(new HashSet<>(user.getRoles()));
        updatedUser.setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        return updatedUser;
    }

    @Override
    public UserInfoDTO toUserInfoDTOFromUser(User user) {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setFirstName(user.getFirstName());
        userInfoDTO.setLastName(user.getLastName());
        userInfoDTO.setEmail(user.getEmail());
        return userInfoDTO;
    }

    @Override
    public User toUserFromUpdateUserRoleDTO(User user, UpdateUserRoleDTO updateUserRoleDTO) {

        Role newRole = roleRepository.findByName(updateUserRoleDTO.getRole())
                .orElseThrow(() -> new NotFoundException("Роль не найдена"));
        Set<Role> roles = new HashSet<>(user.getRoles());
        if (!roles.contains(newRole)) {
            roles.add(newRole);

            user.setRoles(roles);
        }
        return user;
    }
}
