package com.example.demo.Service;

import com.example.demo.DTO.CreateUserResponseDTO;
import com.example.demo.DTO.UpdateUserDTO;
import com.example.demo.DTO.UserInfoDTO;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Feign.UserClient;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;
import feign.FeignException;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserClient userClient;



    @Autowired
    public UserService(UserMapper userMapper,
                       UserRepository userRepository,
                       UserClient userClient) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userClient = userClient;
    }

    @Transactional
    public UserInfoDTO updateUser(Long userId, UpdateUserDTO updateUserDTO) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с ID " + userId + " не найден"));

        user.setFirstName(updateUserDTO.getFirstName());
        user.setLastName(updateUserDTO.getLastName());
        User updatedUser = userRepository.save(user);
        return userMapper.toUserInfoDTO(updatedUser);
    }

    @Transactional
    public User saveUser(SignupRequest signupRequest) throws BadRequestException {
        Long userId = null;
        try {
            CreateUserResponseDTO createUserResponseDTO = userClient.createUser(userMapper.toCreateUserRequestDTO(signupRequest));
            userId = createUserResponseDTO.getUserId();
        } catch (FeignException.Conflict e) {
            throw new BadRequestException("Пользователь с email " + signupRequest.getEmail() + " уже зарегистрирован");
        }
        User user = new User();

        user.setUserId(userId);
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());

        return userRepository.save(user);
    }

}