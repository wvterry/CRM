package com.example.demo.Service;

import com.example.demo.DTO.UpdateUserDTO;
import com.example.demo.DTO.UserInfoDTO;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.Account;
import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AccountService accountService;



    @Autowired
    public UserService(UserMapper userMapper,
                       UserRepository userRepository,
                       AccountService accountService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.accountService = accountService;
    }

    @Transactional
    public UserInfoDTO updateUser(Long userId, UpdateUserDTO updateUserDTO) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с ID " + userId + " не найден"));

        User updatedUser = userRepository.save(userMapper.toUser(user, updateUserDTO));

        return userMapper.toUserInfoDTO(updatedUser);
    }

}