package com.example.demo.Service;

import com.example.demo.DTO.CreateUserRequestDTO;
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
import feign.Request;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {

    private static final Long USER_ID_1 = 1L;
    private static final User USER_1 = new User(USER_ID_1, "Egor", "Zhukov");
    private static final UpdateUserDTO UPDATE_USER_DTO_1 = new UpdateUserDTO("Egor", "Zhukov");
    private static final UserInfoDTO USER_INFO_DTO_1 = new UserInfoDTO("Ivan", "Ivanov");
    private static final SignupRequest SIGNUP_REQUEST =
            new SignupRequest("Egor", "Zhukov", "test@example.com", "password");
    private static final CreateUserResponseDTO CREATE_USER_RESPONSE = new CreateUserResponseDTO(USER_ID_1);
    private static final User SAVED_USER = new User(USER_ID_1, "Egor", "Zhukov");

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserClient userClient;

    @Test
    void updateUserTest() {
        //Arrange
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(USER_1));
        when(userRepository.save(USER_1)).thenReturn(USER_1);
        when(userMapper.toUserInfoDTO(USER_1)).thenReturn(USER_INFO_DTO_1);

        //Act
        UserInfoDTO result = userService.updateUser(USER_ID_1, UPDATE_USER_DTO_1);

        //Assert
        assertNotNull(result);
        assertEquals(result, USER_INFO_DTO_1);
        verify(userRepository).findById(USER_ID_1);
        verify(userRepository).save(USER_1);
        verify(userMapper).toUserInfoDTO(USER_1);
    }

    @Test
    void updateUserTest_Exception() {
        //Arrange
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () -> userService.updateUser(USER_ID_1, UPDATE_USER_DTO_1));
        verify(userRepository).findById(USER_ID_1);
    }

    @Test
    void saveUserTest() throws BadRequestException {
        // Arrange
        when(userClient.createUser(any())).thenReturn(CREATE_USER_RESPONSE);
        when(userRepository.save(any(User.class))).thenReturn(SAVED_USER);
        when(userMapper.toCreateUserRequestDTO(SIGNUP_REQUEST)).thenReturn(new CreateUserRequestDTO());

        // Act
        User result = userService.saveUser(SIGNUP_REQUEST);

        // Assert
        assertNotNull(result);
        assertEquals(USER_ID_1, result.getUserId());
        assertEquals("Egor", result.getFirstName());
        assertEquals("Zhukov", result.getLastName());

        verify(userClient).createUser(any());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toCreateUserRequestDTO(SIGNUP_REQUEST);
    }

    @Test
    void saveUserTest_ConflictException() {
        // Arrange
        Request request = Request.create(
                Request.HttpMethod.POST,
                "http://example.com/api/users",
                Map.of("Content-Type", Collections.singletonList("application/json")),
                "{}".getBytes(),
                null,
                null
        );

        FeignException.Conflict conflictException = new FeignException.Conflict(
                "Conflict",
                request,
                "{}".getBytes(),
                null
        );

        when(userMapper.toCreateUserRequestDTO(SIGNUP_REQUEST)).thenReturn(new CreateUserRequestDTO());
        when(userClient.createUser(any())).thenThrow(conflictException);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.saveUser(SIGNUP_REQUEST));

        assertEquals("Пользователь с email test@example.com уже зарегистрирован", exception.getMessage());

        verify(userClient).createUser(any());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper).toCreateUserRequestDTO(SIGNUP_REQUEST);
    }

    @Test
    void saveUserTest_OtherFeignException() {
        // Arrange
        Request request = Request.create(
                Request.HttpMethod.POST,
                "http://example.com/api/users",
                Map.of("Content-Type", Collections.singletonList("application/json")),
                "{}".getBytes(),
                null,
                null
        );

        FeignException.BadRequest badRequestException = new FeignException.BadRequest(
                "Bad Request",
                request,
                "{}".getBytes(),
                null
        );

        when(userMapper.toCreateUserRequestDTO(SIGNUP_REQUEST)).thenReturn(new CreateUserRequestDTO());
        when(userClient.createUser(any())).thenThrow(badRequestException);

        // Act & Assert
        assertThrows(FeignException.BadRequest.class,
                () -> userService.saveUser(SIGNUP_REQUEST));

        verify(userClient).createUser(any());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper).toCreateUserRequestDTO(SIGNUP_REQUEST);
    }

}
