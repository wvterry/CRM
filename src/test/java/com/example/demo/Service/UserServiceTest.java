package com.example.demo.Service;

import com.example.demo.DTO.*;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Exception.UserAlreadyExistsException;
import com.example.demo.JWT.AuthRequest;
import com.example.demo.JWT.JwtUtil;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.Role;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {

    private static final String email = "test@example.com";
    private static final String password = "password";
    private static final String encodedPassword = "encoded_password";
    private static final String newPassword = "password1";
    private static final String expectedToken = "mocked-jwt-token";
    private static final UpdatePasswordDTO UPDATE_PASSWORD_DTO = new UpdatePasswordDTO(
            password,
            newPassword
    );

    private static final Role USER_ROLE = new Role("USER");
    private static final Role MANAGER_ROLE = new Role("MANAGER");


    private static final Long USER_ID_1 = 1L;
    private static final SignupRequest SIGNUP_REQUEST = new SignupRequest(
            "Egor",
            "Zhukov",
            email,
            password
    );

    private final static com.example.demo.Model.User USER_1 = new com.example.demo.Model.User(
            USER_ID_1,
            "Egor",
            "Zhukov",
            email,
            password
    );

    private static final Long USER_ID_11 = 11L;
    private static final Long USER_ID_22 = 22L;
    private static final Long USER_ID_33 = 33L;
    private static final Set<Role> roles = new HashSet<>();

    private final static com.example.demo.Model.User USER_11 = new com.example.demo.Model.User(
            USER_ID_11,
            "Egor",
            "Zhukov",
            "ez@test.ru",
            "ez"
    );

    private final static com.example.demo.Model.User USER_33 = new com.example.demo.Model.User(
            USER_ID_33,
            roles
    );

    private final static com.example.demo.Model.User USER_22 = new com.example.demo.Model.User(
            USER_ID_22,
            "Ivan",
            "Ivanov",
            "ii@test.ru",
            "ii"
    );
    private final static UpdateUserDTO UPDATE_USER_DTO = new UpdateUserDTO(
            "Egor",
            "Zhukov",
            "ez@test.ru"
    );


    private final static UserInfoDTO USER_INFO_DTO_22 = new UserInfoDTO(
            "Ivan",
            "Ivanov",
            "ii@test.ru"
    );

    private final static UserInfoDTO USER_INFO_DTO_11 = new UserInfoDTO(
            "Egor",
            "Zhukov",
            "ez@test.ru"
    );

    private final static UpdateUserRoleDTO UPDATE_USER_ROLE_DTO = new UpdateUserRoleDTO(
            "MANAGER"
    );

    @InjectMocks
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private AuthRequest authRequest;
    @Mock
    private UserDetails userDetails;
    @Mock
    private Authentication authentication;

    @Mock
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest(email, password);

        userDetails = User.withUsername(email)
                .password("encoded-password")
                .authorities(Collections.emptyList())
                .build();

        authentication = mock(Authentication.class);
    }

    @Test
    void authenticateTest() {
        // Arrange
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(eq(email), argThat(collection -> collection.equals(authorities))))
                .thenReturn(expectedToken);

        // Act
        String result = userService.authenticate(authRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedToken, result);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(eq(email), anyCollection());
    }

    @Test
    void registerTest(){
        //Arrange
        when(userRepository.existsByEmail(SIGNUP_REQUEST.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(USER_ROLE));
        when(userMapper.toUser(Set.of(USER_ROLE), encodedPassword, SIGNUP_REQUEST)).thenReturn(USER_1);

        //Act
        userService.register(SIGNUP_REQUEST);

        //Assert
        verify(userRepository).existsByEmail(SIGNUP_REQUEST.getEmail());
        verify(passwordEncoder).encode(password);
        verify(roleRepository).findByName("USER");
        verify(userMapper).toUser(Set.of(USER_ROLE), encodedPassword, SIGNUP_REQUEST);
        verify(userRepository).save(USER_1);
    }

    @Test
    void registerTest_UserExist(){
        //Arrange
        when(userRepository.existsByEmail(SIGNUP_REQUEST.getEmail())).thenReturn(true);

        //Assert
        assertThrows(UserAlreadyExistsException.class, () -> userService.register(SIGNUP_REQUEST));
        verify(userRepository).existsByEmail(SIGNUP_REQUEST.getEmail());
    }

    @Test
    void updatePassTest(){
        //Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(USER_1));
        when(passwordEncoder.matches(password, USER_1.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userMapper.toUser(encodedPassword, USER_1, UPDATE_PASSWORD_DTO)).thenReturn(USER_1);

        //Act
        userService.updatePass(email,UPDATE_PASSWORD_DTO);

        //Assert
        verify(userRepository).findUserByEmail(email);
        verify(passwordEncoder).matches(password, USER_1.getPassword());
        verify(passwordEncoder).encode(newPassword);
        verify(userMapper).toUser(encodedPassword, USER_1, UPDATE_PASSWORD_DTO);
        verify(userRepository).save(USER_1);
    }

    @Test
    void updatePassTest_Exception(){
        //Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(USER_1));
        when(passwordEncoder.matches(password, USER_1.getPassword())).thenReturn(false);

        //Assert
        assertThrows(RuntimeException.class, () ->
                userService.updatePass(email, UPDATE_PASSWORD_DTO));
    }

    @Test
    void updateUserTest_UserExist(){
        //Arrange
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(USER_1));
        when(passwordEncoder.encode(USER_1.getPassword())).thenReturn(encodedPassword);
        when(userMapper.toUser(encodedPassword, USER_1, UPDATE_USER_DTO)).thenReturn(USER_1);
        when(userMapper.toUserInfoDTO(USER_1)).thenReturn(USER_INFO_DTO_11);

        //Act
        UserInfoDTO result = userService.updateUser(USER_ID_1, UPDATE_USER_DTO);

        //Assert
        assertNotNull(result);
        assertEquals(result, USER_INFO_DTO_11);
        verify(userRepository).findById(USER_ID_1);
        verify(userMapper).toUser(encodedPassword, USER_1, UPDATE_USER_DTO);
        verify(userMapper).toUserInfoDTO(USER_1);
    }

    @Test
    void updateUserTest_UserNotFound(){
        //Arrange
        when(userRepository.findById(USER_ID_11)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () -> userService.updateUser(USER_ID_11, UPDATE_USER_DTO));
        verify(userRepository).findById(USER_ID_11);
    }

    @Test
    void getAllUsersTest(){
        //Arrange
        when(userRepository.findAll()).thenReturn(List.of(USER_11, USER_22));
        when(userMapper.toUserInfoDTO(USER_11)).thenReturn(USER_INFO_DTO_11);
        when(userMapper.toUserInfoDTO(USER_22)).thenReturn(USER_INFO_DTO_22);

        //Act
        List<UserInfoDTO> result = userService.getAllUsersInfo();

        //Assert
        assertNotNull(result);
        assertEquals(result, List.of(USER_INFO_DTO_11, USER_INFO_DTO_22));
        verify(userRepository).findAll();
        verify(userMapper).toUserInfoDTO(USER_11);
        verify(userMapper).toUserInfoDTO(USER_22);
    }

    @Test
    void updateUserRoleTest_UserExist(){
        //Arrange
        when(userRepository.findById(USER_ID_33)).thenReturn(Optional.of(USER_33));
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(MANAGER_ROLE));

        //Act
        userService.updateUserRole(USER_ID_33, UPDATE_USER_ROLE_DTO);

        //Assert
        verify(userRepository).findById(USER_ID_33);
        verify(roleRepository).findByName("MANAGER");
    }

    @Test
    void updateUserRoleTest_UserNotFound(){
        //Arrange
        when(userRepository.findById(USER_ID_11)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () -> userService.updateUserRole(USER_ID_11, UPDATE_USER_ROLE_DTO));
        verify(userRepository).findById(USER_ID_11);
    }

    @Test
    void updateUserRoleTest_RoleNotFound(){
        //Arrange
        when(userRepository.findById(USER_ID_33)).thenReturn(Optional.of(USER_33));
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () -> userService.updateUserRole(USER_ID_33, UPDATE_USER_ROLE_DTO));
        verify(userRepository).findById(USER_ID_33);
        verify(roleRepository).findByName("MANAGER");
    }
}