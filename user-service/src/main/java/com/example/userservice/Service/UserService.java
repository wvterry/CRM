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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final TransactionTemplate transactionTemplate;

    @Autowired
    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       KafkaTemplate<String, Object> kafkaTemplate,
                       TransactionTemplate transactionTemplate) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    public UserInfoDTO updateUser(Long userId, UpdateUserDTO updateUserDTO) {
        UserInfoDTO userInfoDTO = transactionTemplate.execute(status -> {

                    User user = userRepository.findById(userId).orElseThrow(() ->
                            new NotFoundException("Пользователь с id " + userId + " не найден"));

                    user.setFirstName(updateUserDTO.getFirstName());
                    user.setLastName(updateUserDTO.getLastName());
                    userRepository.save(user);

            return userMapper.toUserInfoDTO(user);
                });

        UpdateUserEvent userEvent = new UpdateUserEvent(
                "UPDATE_USER",
                userId,
                updateUserDTO);

        kafkaTemplate.send("user_updated", userId.toString(), userEvent);

        return userInfoDTO;
    }

    @Transactional
    public UserIdResponseDTO createUser(CreateUserDTO createUserDTO) {
        User user = userRepository.save(userMapper.toUser(createUserDTO));
        return userMapper.toUserIdResponseDTO(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        transactionTemplate.executeWithoutResult(transactionStatus -> {User user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + id + " не найден"));
            userRepository.delete(user);
        });

        DeleteUserEvent deleteUserEvent = new DeleteUserEvent(id);
        kafkaTemplate.send("user_deleted", id.toString(), deleteUserEvent);
    }

    @Transactional(readOnly = true)
    public List<UserInfoDTO> getAll() {
        return userRepository.findAll().stream().map(userMapper::toUserInfoDTO).toList();
    }

    @Transactional(readOnly = true)
    public UserInfoDTO getById(Long id){
        User user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + id + " не найден"));
        return userMapper.toUserInfoDTO(user);
    }


}
