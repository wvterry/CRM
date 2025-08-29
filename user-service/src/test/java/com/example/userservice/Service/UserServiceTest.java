package com.example.userservice.Service;

import com.example.userservice.DTO.CreateUserDTO;
import com.example.userservice.DTO.UpdateUserDTO;
import com.example.userservice.DTO.UserIdResponseDTO;
import com.example.userservice.DTO.UserInfoDTO;
import com.example.userservice.Exception.NotFoundException;
import com.example.userservice.Mapper.UserMapper;
import com.example.userservice.Model.User;
import com.example.userservice.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private TransactionTemplate transactionTemplate;

    private final static Long ID1 = 1L;
    private final static User USER1 = new User(1L, "Ivan", "Ivanov");
    private final static UserInfoDTO USER_INFO_DTO1 = new UserInfoDTO("Ivan", "Ivanov");
    private final static CreateUserDTO CREATE_USER_DTO_1 = new CreateUserDTO("Ivan", "Ivanov");
    private final static UserIdResponseDTO USER_ID_RESPONSE_DTO_1 = new UserIdResponseDTO(1L);
    private final static UpdateUserDTO UPDATE_USER_DTO_1 = new UpdateUserDTO("Ivan", "Ivanov");
    private final static User USER2 = new User(2L, "Egor", "Egorov");
    private final static UserInfoDTO USER_INFO_DTO2 = new UserInfoDTO("Egor", "Egorov");

    @Test
    void getAllUsersTest() {
        //Arrange
        when(userRepository.findAll()).thenReturn(List.of(USER1, USER2));
        when(userMapper.toUserInfoDTO(USER1)).thenReturn(USER_INFO_DTO1);
        when(userMapper.toUserInfoDTO(USER2)).thenReturn(USER_INFO_DTO2);

        //Act
        List<UserInfoDTO> result = userService.getAll();

        //Assert
        assertNotNull(result);
        assertEquals(result, List.of(USER_INFO_DTO1, USER_INFO_DTO2));
        verify(userRepository).findAll();
        verify(userMapper).toUserInfoDTO(USER1);
        verify(userMapper).toUserInfoDTO(USER2);
    }

//    @Test
//    void deleteUserTest_Exception() {
//        // Arrange
//        when(userRepository.findById(ID1)).thenReturn(Optional.empty());
//        doAnswer(invocation -> {
//            User user = userRepository.findById(ID1).orElseThrow(
//                    () -> new NotFoundException("Пользователь с id " + ID1 + " не найден"));
//            userRepository.delete(user);
//            return null;
//        }).when(transactionTemplate).executeWithoutResult(any());
//        NotFoundException exception = assertThrows(NotFoundException.class,
//                () -> userService.deleteUser(ID1));
//
//        // Assert
//        assertEquals("Пользователь с id " + ID1 + " не найден", exception.getMessage());
//        verify(userRepository).findById(ID1);
//        verify(transactionTemplate).executeWithoutResult(any());
//        verify(kafkaTemplate, never()).send(any(), any(), any());
//    }

//    @Test
//    void deleteUserTest() {
//        // Arrange
//        when(userRepository.findById(ID1)).thenReturn(Optional.of(USER1));
//        doAnswer(invocation -> {
//            User user = userRepository.findById(ID1).orElseThrow();
//            userRepository.delete(user);
//            return null;
//        }).when(transactionTemplate).executeWithoutResult(any());
//
//        // Act
//        userService.deleteUser(ID1);
//
//        // Assert
//        verify(userRepository).findById(ID1);
//        verify(userRepository).delete(USER1);
//        verify(transactionTemplate).executeWithoutResult(any());
//        verify(kafkaTemplate).send(eq("user_deleted"), eq(ID1.toString()), any(DeleteUserEvent.class));
//    }

    @Test
    void createUserTest() {
        //Arrange
        when(userMapper.toUser(CREATE_USER_DTO_1)).thenReturn(USER1);
        when(userRepository.save(USER1)).thenReturn(USER1);
        when(userMapper.toUserIdResponseDTO(USER1)).thenReturn(USER_ID_RESPONSE_DTO_1);

        //Act
        UserIdResponseDTO result = userService.createUser(CREATE_USER_DTO_1);

        //Assert
        assertNotNull(result);
        verify(userMapper).toUser(CREATE_USER_DTO_1);
        verify(userMapper).toUserIdResponseDTO(USER1);
        verify(userRepository).save(USER1);
    }

//    @Test
//    void updateUser() {
//        //Arrange
//        when(userRepository.findById(ID1)).thenReturn(Optional.of(USER1));
//        when(userRepository.save(USER1)).thenReturn(USER1);
//        when(userMapper.toUserInfoDTO(USER1)).thenReturn(USER_INFO_DTO1);
//        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
//            User user = userRepository.findById(ID1).orElseThrow();
//            user.setFirstName(UPDATE_USER_DTO_1.getFirstName());
//            user.setLastName(UPDATE_USER_DTO_1.getLastName());
//            userRepository.save(user);
//            return userMapper.toUserInfoDTO(user);
//        });
//
//        //Act
//        UserInfoDTO result = userService.updateUser(ID1, UPDATE_USER_DTO_1);
//
//        //Assert
//        assertNotNull(result);
//        assertEquals(USER_INFO_DTO1, result);
//        verify(userRepository).findById(ID1);
//        verify(userRepository).save(USER1);
//        verify(userMapper).toUserInfoDTO(USER1);
//        verify(transactionTemplate).execute(any());
//        verify(kafkaTemplate).send(eq("user_updated"), eq(ID1.toString()), any(UpdateUserEvent.class));
//    }
//
//    @Test
//    void updateUser_Exception() {
//        //Arrange
//        when(userRepository.findById(ID1)).thenReturn(Optional.empty());
//        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
//            User user = userRepository.findById(ID1).orElseThrow(() ->
//                    new NotFoundException("Пользователь с id " + ID1 + " не найден"));
//            return userMapper.toUserInfoDTO(user);
//        });
//
//        //Act & Assert
//        NotFoundException exception = assertThrows(NotFoundException.class,
//                () -> userService.updateUser(ID1, UPDATE_USER_DTO_1));
//        assertEquals("Пользователь с id " + ID1 + " не найден", exception.getMessage());
//        verify(userRepository).findById(ID1);
//        verify(transactionTemplate).execute(any());
//    }

    @Test
    void getAllTest() {
        //Arrange
        when(userRepository.findAll()).thenReturn(List.of(USER1));
        when(userMapper.toUserInfoDTO(USER1)).thenReturn(USER_INFO_DTO1);

        //Act
        List<UserInfoDTO> result = userService.getAll();

        //Assert
        assertEquals(result, List.of(USER_INFO_DTO1));
        verify(userRepository).findAll();
        verify(userMapper).toUserInfoDTO(USER1);
    }

    @Test
    void getByIdTest() {
        //Arrange
        when(userRepository.findById(ID1)).thenReturn(Optional.of(USER1));
        when(userMapper.toUserInfoDTO(USER1)).thenReturn(USER_INFO_DTO1);

        //Act
        UserInfoDTO result = userService.getById(ID1);

        //Assert
        assertEquals(result, USER_INFO_DTO1);
        verify(userRepository).findById(ID1);
        verify(userMapper).toUserInfoDTO(USER1);
    }

    @Test
    void getById_UserNotFound() {
        //Arrange
        when(userRepository.findById(ID1)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () -> userService.getById(ID1));
        verify(userRepository).findById(ID1);
    }
}
