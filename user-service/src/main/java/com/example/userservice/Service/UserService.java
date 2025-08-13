package com.example.userservice.Service;

import com.example.userservice.DTO.CreateUserDTO;
import com.example.userservice.DTO.UpdateUserDTO;
import com.example.userservice.DTO.UserIdResponseDTO;
import com.example.userservice.DTO.UserInfoDTO;
import com.example.userservice.Event.DeleteUserEvent;
import com.example.userservice.Event.UpdateUserEvent;
import com.example.userservice.Exception.NotFoundException;
import com.example.userservice.Mapper.UserMapper;
import com.example.userservice.Model.User;
import com.example.userservice.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper, KafkaTemplate<String, Object> kafkaTemplate) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public UserInfoDTO updateUser(Long userId, UpdateUserDTO updateUserDTO){
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));
        userRepository.save(userMapper.toUser(user, updateUserDTO));

                UpdateUserEvent userEvent = new UpdateUserEvent(
                "UPDATE_USER",
                        userId,
                updateUserDTO);

        kafkaTemplate.send("user_updated", userId.toString(), userEvent);

        return userMapper.toUserInfoDTO(user);
    }

    @Transactional
    public UserIdResponseDTO createUser(CreateUserDTO createUserDTO){
       User user = userRepository.save(userMapper.toUser(createUserDTO));
       return userMapper.toUserIdResponseDTO(user);
    }

    @Transactional
    public void deleteUser(Long id){
        User user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + id + " не найден"));
    userRepository.delete(user);

        DeleteUserEvent deleteUserEvent = new DeleteUserEvent("DELETE_USER", id);
        kafkaTemplate.send("user_deleted", id.toString(), deleteUserEvent);
    }

    @Transactional(readOnly = true)
    public List<UserInfoDTO> getAll(){
        return userRepository.findAll().stream().map(userMapper::toUserInfoDTO).toList();
    }


}
