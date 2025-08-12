package com.example.demo.Service;

import com.example.demo.DTO.*;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Feign.UserClient;
import com.example.demo.JWT.AuthRequest;
import com.example.demo.JWT.JwtUtil;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Mapper.AccountMapper;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.Account;
import com.example.demo.Model.Role;
import com.example.demo.Repository.AccountRepository;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import feign.FeignException;
import feign.Request;
import org.apache.coyote.BadRequestException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AccountServiceTest {

    private static final String EMAIL = "test@example.com";
    private static final String EMAIL_1 = "test1@example.com";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encoded_password";
    private static final String NEW_PASSWORD = "password1";
    private static final String EXPECTED_TOKEN= "mocked-jwt-token";
    private static final Long USER_ID_1= 1L;
    private static final Long ACCOUNT_ID_1= 1L;
    private static final Long ACCOUNT_ID_2= 2L;


    private static final Role USER_ROLE = new Role("USER");
    private static final Role MANAGER_ROLE = new Role("MANAGER");

    private static final UpdatePasswordDTO UPDATE_PASSWORD_DTO_1 = new UpdatePasswordDTO(
            PASSWORD,
            NEW_PASSWORD);

    private static final UpdateAccountRoleDTO UPDATE_ACCOUNT_ROLE_DTO = new UpdateAccountRoleDTO("MANAGER");

    private static final UpdateEmailDTO UPDATE_EMAIL_DTO = new UpdateEmailDTO(EMAIL_1);

    private static final SignupRequest SIGNUP_REQUEST = new SignupRequest(
            "Egor",
            "Zhukov",
            EMAIL,
            PASSWORD
    );

    private final static com.example.demo.Model.User USER_1 = new com.example.demo.Model.User(
            USER_ID_1,
            "Egor",
            "Zhukov"
    );

    private final static CreateUserResponseDTO USER_ID_RESPONSE_DTO = new CreateUserResponseDTO(USER_ID_1);
    private final static CreateUserRequestDTO CREATE_USER_REQUEST_DTO = new CreateUserRequestDTO("Egor", "Zhukov");

    private final static Account ACCOUNT_1 = new Account(ACCOUNT_ID_1, EMAIL, USER_1, Set.of(USER_ROLE));
    private final static Account ACCOUNT_2 = new Account(ACCOUNT_ID_2, EMAIL_1, PASSWORD);



    @InjectMocks
    private AccountService accountService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserClient userClient;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthRequest authRequest;
    @Mock
    private UserDetails userDetails;
    @Mock
    private Authentication authentication;
    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest(EMAIL, PASSWORD);

        userDetails = User.withUsername(EMAIL)
                .password("encoded-password")
                .authorities(Collections.emptyList())
                .build();

        authentication = mock(Authentication.class);
    }

    @Test
    void authenticateTest(){
        //Arrange
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(eq(EMAIL), argThat(collection -> collection.equals(authorities))))
                .thenReturn(EXPECTED_TOKEN);

        // Act
        String result = accountService.authenticate(authRequest);

        // Assert
        assertNotNull(result);
        assertEquals(EXPECTED_TOKEN, result);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(eq(EMAIL), anyCollection());
    }

    @Test
    void register_Successful() throws Exception {
        // Arrange
        when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userMapper.toCreateUserRequestDTO(SIGNUP_REQUEST)).thenReturn(CREATE_USER_REQUEST_DTO);
        when(userClient.createUser(CREATE_USER_REQUEST_DTO)).thenReturn(USER_ID_RESPONSE_DTO);
        when(userMapper.toUser(USER_ID_1, SIGNUP_REQUEST)).thenReturn(USER_1);
        when(userRepository.save(USER_1)).thenReturn(USER_1);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(USER_ROLE));

        Account expectedAccount = new Account();
        when(accountMapper.toAccount(USER_1, USER_ID_1, Set.of(USER_ROLE), ENCODED_PASSWORD, SIGNUP_REQUEST))
                .thenReturn(expectedAccount);
        when(accountRepository.save(expectedAccount)).thenReturn(expectedAccount);

        // Act
        accountService.register(SIGNUP_REQUEST);

        // Assert
        verify(accountRepository).existsByEmail(EMAIL);
        verify(userClient).createUser(CREATE_USER_REQUEST_DTO);
        verify(userRepository).save(USER_1);
        verify(roleRepository).findByName("USER");
        verify(accountRepository).save(expectedAccount);
    }

    @Test
    void register_Exception_EmailAlreadyExist(){
        // Arrange
        when(accountRepository.existsByEmail(EMAIL)).thenReturn(true);

        // Assert
        assertThrows(BadRequestException.class, () -> accountService.register(SIGNUP_REQUEST));
        verify(accountRepository).existsByEmail(EMAIL);
    }

    @Test
    void register_Exception_UserClientThrowsConflict(){
        // Arrange
        when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userMapper.toCreateUserRequestDTO(SIGNUP_REQUEST)).thenReturn(CREATE_USER_REQUEST_DTO);
        when(userClient.createUser(CREATE_USER_REQUEST_DTO)).thenThrow(new
                FeignException.Conflict("Conflict", mock(Request.class), null, null));

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> accountService.register(SIGNUP_REQUEST));
        assertEquals("Пользователь с email " + EMAIL + " уже зарегистрирован", exception.getMessage());

        // Assert
        verify(accountRepository).existsByEmail(EMAIL);
        verify(userClient).createUser(CREATE_USER_REQUEST_DTO);
        verify(userMapper).toCreateUserRequestDTO(SIGNUP_REQUEST);
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(userRepository, roleRepository, accountMapper);
    }

    @Test
    void register_Exception_RoleNotFound() throws BadRequestException {
        // Arrange
        when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userMapper.toCreateUserRequestDTO(SIGNUP_REQUEST)).thenReturn(CREATE_USER_REQUEST_DTO);
        when(userClient.createUser(CREATE_USER_REQUEST_DTO)).thenReturn(USER_ID_RESPONSE_DTO);
        when(userMapper.toUser(USER_ID_1, SIGNUP_REQUEST)).thenReturn(USER_1);
        when(userRepository.save(USER_1)).thenReturn(USER_1);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () -> accountService.register(SIGNUP_REQUEST));
        verify(accountRepository).existsByEmail(EMAIL);
        verify(userClient).createUser(CREATE_USER_REQUEST_DTO);
        verify(userRepository).save(USER_1);
        verify(roleRepository).findByName("USER");
    }

    @Test
    void register_Exception_AccountSaveFails_RollsBackUserCreation(){
        when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userMapper.toCreateUserRequestDTO(SIGNUP_REQUEST)).thenReturn(CREATE_USER_REQUEST_DTO);
        when(userClient.createUser(CREATE_USER_REQUEST_DTO)).thenReturn(USER_ID_RESPONSE_DTO);
        when(userMapper.toUser(USER_ID_1, SIGNUP_REQUEST)).thenReturn(USER_1);
        when(userRepository.save(USER_1)).thenReturn(USER_1);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(USER_ROLE));
        when(accountMapper.toAccount(USER_1, USER_ID_1, Set.of(USER_ROLE), ENCODED_PASSWORD, SIGNUP_REQUEST)).thenReturn(ACCOUNT_1);
        when(accountRepository.save(ACCOUNT_1)).thenThrow(new RuntimeException("saveEx"));

        //Assert
        assertThrows(RuntimeException.class, () -> accountService.register(SIGNUP_REQUEST));
        verify(userClient).deleteUser(USER_ID_1);
        verify(accountRepository).existsByEmail(EMAIL);
        verify(userClient).createUser(CREATE_USER_REQUEST_DTO);
        verify(userRepository).save(USER_1);
        verify(roleRepository).findByName("USER");
        verify(accountRepository).save(ACCOUNT_1);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void register_Exception_AccountSaveFails_RollsBackException() {
        // Arrange
        when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userMapper.toCreateUserRequestDTO(SIGNUP_REQUEST)).thenReturn(CREATE_USER_REQUEST_DTO);
        when(userClient.createUser(CREATE_USER_REQUEST_DTO)).thenReturn(USER_ID_RESPONSE_DTO);
        when(userMapper.toUser(USER_ID_1, SIGNUP_REQUEST)).thenReturn(USER_1);
        when(userRepository.save(USER_1)).thenReturn(USER_1);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(USER_ROLE));

        Account expectedAccount = new Account();
        when(accountMapper.toAccount(USER_1, USER_ID_1, Set.of(USER_ROLE), ENCODED_PASSWORD, SIGNUP_REQUEST))
                .thenReturn(expectedAccount);
        when(accountRepository.save(expectedAccount)).thenThrow(new RuntimeException("DB save error"));
        doThrow(new RuntimeException("Rollback failed")).when(userClient).deleteUser(USER_ID_1);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> accountService.register(SIGNUP_REQUEST));

        //Assert
        assertEquals("Не удалось удалить пользователя с id " + USER_ID_1 + ". Обратитесь к администратору",
                exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Rollback failed", exception.getCause().getMessage());
        verify(accountRepository).existsByEmail(EMAIL);
        verify(userClient).createUser(CREATE_USER_REQUEST_DTO);
        verify(userRepository).save(USER_1);
        verify(roleRepository).findByName("USER");
        verify(accountMapper).toAccount(USER_1, USER_ID_1, Set.of(USER_ROLE), ENCODED_PASSWORD, SIGNUP_REQUEST);
        verify(accountRepository).save(expectedAccount);
        verify(userClient).deleteUser(USER_ID_1);
        verifyNoMoreInteractions(accountRepository, userRepository, roleRepository);
    }

    @Test
    void updatePass(){
        // Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(ACCOUNT_2));
        when(passwordEncoder.matches(UPDATE_PASSWORD_DTO_1.getPassword(), ACCOUNT_2.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(UPDATE_PASSWORD_DTO_1.getNewPassword())).thenReturn(ENCODED_PASSWORD);
        when(accountMapper.toAccount(ENCODED_PASSWORD, ACCOUNT_2)).thenReturn(ACCOUNT_2);

        // Act
        accountService.updatePass(EMAIL_1, UPDATE_PASSWORD_DTO_1);

        //Assert
        verify(accountRepository).findByEmail(EMAIL_1);
        verify(passwordEncoder).matches(ACCOUNT_2.getPassword(), UPDATE_PASSWORD_DTO_1.getPassword());
        verify(passwordEncoder).encode(UPDATE_PASSWORD_DTO_1.getNewPassword());
        verify(accountMapper).toAccount(ENCODED_PASSWORD, ACCOUNT_2);
    }

    @Test
    void updatePass_AccountNotFound(){
        // Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, ()-> accountService.updatePass(EMAIL_1, UPDATE_PASSWORD_DTO_1));
        verify(accountRepository).findByEmail(EMAIL_1);
    }

    @Test
    void updatePass_IncorrectPass(){
        // Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(ACCOUNT_2));
        when(passwordEncoder.matches(UPDATE_PASSWORD_DTO_1.getPassword(), ACCOUNT_2.getPassword())).thenReturn(false);

        //Assert
        assertThrows(RuntimeException.class, ()-> accountService.updatePass(EMAIL_1, UPDATE_PASSWORD_DTO_1));
        verify(accountRepository).findByEmail(EMAIL_1);
    }

    @Test
    void updateRoleTest(){
        // Arrange
        when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.of(ACCOUNT_1));
        when(roleRepository.findByName(UPDATE_ACCOUNT_ROLE_DTO.getRole())).thenReturn(Optional.of(MANAGER_ROLE));

        //Act
        accountService.updateRole(ACCOUNT_ID_1, UPDATE_ACCOUNT_ROLE_DTO);

        //Assert
        verify(accountRepository).findById(ACCOUNT_ID_1);
        verify(roleRepository).findByName("MANAGER");
        assertTrue(ACCOUNT_1.getRoles().contains(USER_ROLE));
        assertTrue(ACCOUNT_1.getRoles().contains(MANAGER_ROLE));
        verify(accountRepository).save(ACCOUNT_1);
    }

    @Test
    void updateRole_AccountNotFound(){
        // Arrange
        when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, ()-> accountService.updateRole(ACCOUNT_ID_1, UPDATE_ACCOUNT_ROLE_DTO));
        verify(accountRepository).findById(ACCOUNT_ID_1);
    }

    @Test
    void updateRole_RoleNotFound(){
        // Arrange
        when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.of(ACCOUNT_1));
        when(roleRepository.findByName(UPDATE_ACCOUNT_ROLE_DTO.getRole())).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, ()-> accountService.updateRole(ACCOUNT_ID_1, UPDATE_ACCOUNT_ROLE_DTO));
        verify(accountRepository).findById(ACCOUNT_ID_1);
        verify(roleRepository).findByName(UPDATE_ACCOUNT_ROLE_DTO.getRole());
    }

    @Test
    void updateEmail(){
        // Arrange
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(ACCOUNT_1));
        when(userRepository.findById(ACCOUNT_1.getUser().getUserId())).thenReturn(Optional.of(USER_1));

        //Act
        accountService.updateEmail(EMAIL, UPDATE_EMAIL_DTO);

        //Assert
        verify(accountRepository).findByEmail(EMAIL);
        verify(userRepository).findById(ACCOUNT_1.getUser().getUserId());
        assertEquals(EMAIL_1, ACCOUNT_1.getEmail());
        verify(accountRepository).save(ACCOUNT_1);
    }

    @Test
    void updateEmail_AccountNotFound(){
        // Arrange
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, ()-> accountService.updateEmail(EMAIL, UPDATE_EMAIL_DTO));
        verify(accountRepository).findByEmail(EMAIL);
    }

    @Test
    void updateEmail_UserNotFound(){
        //Arrange
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(ACCOUNT_1));
        when(userRepository.findById(ACCOUNT_1.getUser().getUserId())).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, ()-> accountService.updateEmail(EMAIL, UPDATE_EMAIL_DTO));
        verify(accountRepository).findByEmail(EMAIL);
    }

}
