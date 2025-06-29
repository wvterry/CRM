package com.example.demo.Service;

import com.example.demo.DTO.UpdateUserDTO;
import com.example.demo.DTO.UpdateUserRoleDTO;
import com.example.demo.DTO.UpdatedUserDTO;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.Model.User;
import com.example.demo.DTO.UserInfoDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Autowired
    public AdminService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UpdatedUserDTO updateUser(Long userId, UpdateUserDTO updateUserDTO){
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()){
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        userRepository.save(userMapper.toUserFromUpdateUserDTO(optionalUser.get(), updateUserDTO));
        return userMapper.toUpdatedUserDTOFromUser(optionalUser.get());
    }

    @Transactional(readOnly = true)
    public List<UserInfoDTO> getAllUsers(){
        return userRepository
                .findAll()
                .stream()
                .map(userMapper::toUserInfoDTOFromUser)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserRole(Long id, UpdateUserRoleDTO updateUserRoleDTO) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()){
            throw new NotFoundException("Пользователь с ID \" + userId + \" не найден");
        }
        userRepository.save(userMapper.toUserFromUpdateUserRoleDTO(optionalUser.get(), updateUserRoleDTO));

    }
}
