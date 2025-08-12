package com.example.demo.Service;

import com.example.demo.DTO.UpdateUserDTO;
import com.example.demo.DTO.UserInfoDTO;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {

    private static final Long USER_ID_1 = 1L;
    private static final User USER_1 = new User(USER_ID_1, "Egor", "Zhukov");
    private static final UpdateUserDTO UPDATE_USER_DTO_1 = new UpdateUserDTO("Egor", "Zhukov");
    private static final UserInfoDTO USER_INFO_DTO_1 = new UserInfoDTO("Ivan", "Ivanov");



    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;



    @Test
    void updateUserTest(){
        //Arrange
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(USER_1));
        when(userMapper.toUser(USER_1, UPDATE_USER_DTO_1)).thenReturn(USER_1);
        when(userRepository.save(USER_1)).thenReturn(USER_1);
        when(userMapper.toUserInfoDTO(USER_1)).thenReturn(USER_INFO_DTO_1);

        //Act
        UserInfoDTO result = userService.updateUser(USER_ID_1, UPDATE_USER_DTO_1);

        //Assert
        assertNotNull(result);
        assertEquals(result, USER_INFO_DTO_1);
        verify(userRepository).findById(USER_ID_1);
        verify(userMapper).toUser(USER_1, UPDATE_USER_DTO_1);
        verify(userRepository).save(USER_1);
        verify(userMapper).toUserInfoDTO(USER_1);
    }

    @Test
    void updateUserTest_Exception(){
        //Arrange
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, ()-> userService.updateUser(USER_ID_1, UPDATE_USER_DTO_1));
        verify(userRepository).findById(USER_ID_1);
    }

}
