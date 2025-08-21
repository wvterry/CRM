package com.example.demo.Service;

import com.example.demo.DTO.*;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Feign.UserClient;
import com.example.demo.JWT.AuthRequest;
import com.example.demo.JWT.JwtTokenService;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Mapper.AccountMapper;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.Account;
import com.example.demo.Model.Role;
import com.example.demo.Repository.AccountRepository;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
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
import org.springframework.transaction.support.TransactionTemplate;

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
    private final static Account ACCOUNT_2 = new Account(ACCOUNT_ID_2, EMAIL_1, ENCODED_PASSWORD);

    private final static AccountInfoDTO ACCOUNT_INFO_DTO =
            new AccountInfoDTO(ACCOUNT_ID_1, EMAIL, USER_1.getFirstName(), USER_1.getLastName());


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
    private JwtTokenService jwtUtil;
    @Mock
    private AuthRequest authRequest;
    @Mock
    private UserDetails userDetails;
    @Mock
    private Authentication authentication;
    @Mock
    private UserService userService;
    @Mock
    private TransactionTemplate transactionTemplate;
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
    void register_Success() throws BadRequestException {
        // Arrange
        when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userService.saveUser(SIGNUP_REQUEST)).thenReturn(USER_1);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(USER_ROLE));
        doNothing().when(transactionTemplate).executeWithoutResult(any());

        // Act
        assertDoesNotThrow(() -> accountService.register(SIGNUP_REQUEST));

        // Assert
        verify(accountRepository).existsByEmail(EMAIL);
        verify(userService).saveUser(SIGNUP_REQUEST);
        verify(passwordEncoder).encode(PASSWORD);
        verify(roleRepository).findByName("USER");
        verify(transactionTemplate).executeWithoutResult(any());
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
    void register_Exception_UserClientThrowsConflict() throws BadRequestException {
        // Arrange
        when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userMapper.toCreateUserRequestDTO(SIGNUP_REQUEST)).thenReturn(CREATE_USER_REQUEST_DTO);
        when(userService.saveUser(SIGNUP_REQUEST)).thenThrow(
                new BadRequestException("Пользователь с email " + EMAIL + " уже зарегистрирован")
        );

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> accountService.register(SIGNUP_REQUEST));
        assertEquals("Пользователь с email " + EMAIL + " уже зарегистрирован", exception.getMessage());

        // Assert
        verify(accountRepository).existsByEmail(EMAIL);
        verify(userService).saveUser(SIGNUP_REQUEST);
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(passwordEncoder, roleRepository, accountMapper, transactionTemplate);
    }

    @Test
    void register_Exception_RoleNotFound() throws BadRequestException {
        // Arrange
        when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userService.saveUser(SIGNUP_REQUEST)).thenReturn(USER_1);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> accountService.register(SIGNUP_REQUEST));
        // Assert
        assertEquals("Роль отсутствует", exception.getMessage());
        verify(accountRepository).existsByEmail(EMAIL);
        verify(userService).saveUser(SIGNUP_REQUEST);
        verify(passwordEncoder).encode(PASSWORD);
        verify(roleRepository).findByName("USER");
        verifyNoInteractions(userClient, userRepository, transactionTemplate);
    }

    @Test
    void register_Exception_AccountSaveFails_RollsBackUserCreation() throws BadRequestException {
        // Arrange
        when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userService.saveUser(SIGNUP_REQUEST)).thenReturn(USER_1);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(USER_ROLE));
        doThrow(new RuntimeException("saveEx"))
                .when(transactionTemplate).executeWithoutResult(any());
        doNothing().when(userClient).deleteUser(USER_ID_1);

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.register(SIGNUP_REQUEST));

        // Assert
        assertEquals("saveEx", exception.getMessage());
        verify(accountRepository).existsByEmail(EMAIL);
        verify(userService).saveUser(SIGNUP_REQUEST);
        verify(passwordEncoder).encode(PASSWORD);
        verify(roleRepository).findByName("USER");
        verify(transactionTemplate).executeWithoutResult(any());
        verify(userClient).deleteUser(USER_ID_1);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_Exception_AccountSaveFails_RollsBackException() throws BadRequestException {
        // Arrange
        when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userService.saveUser(SIGNUP_REQUEST)).thenReturn(USER_1);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(USER_ROLE));
        doThrow(new RuntimeException("DB save error"))
                .when(transactionTemplate).executeWithoutResult(any());
        doThrow(new RuntimeException("Rollback failed"))
                .when(userClient).deleteUser(USER_ID_1);

        // Act
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> accountService.register(SIGNUP_REQUEST));

        // Assert
        assertEquals("Не удалось удалить пользователя с id " + USER_ID_1 + ". Обратитесь к администратору",
                exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Rollback failed", exception.getCause().getMessage());
        verify(accountRepository).existsByEmail(EMAIL);
        verify(userService).saveUser(SIGNUP_REQUEST);
        verify(passwordEncoder).encode(PASSWORD);
        verify(roleRepository).findByName("USER");
        verify(transactionTemplate).executeWithoutResult(any());
        verify(userClient).deleteUser(USER_ID_1);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updatePass() throws BadRequestException {
        // Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(ACCOUNT_2));
        when(passwordEncoder.matches(UPDATE_PASSWORD_DTO_1.getPassword(), ACCOUNT_2.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(UPDATE_PASSWORD_DTO_1.getNewPassword())).thenReturn(ENCODED_PASSWORD);

        // Act
        accountService.updatePass(EMAIL_1, UPDATE_PASSWORD_DTO_1);

        // Assert
        verify(accountRepository).findByEmail(EMAIL_1);
        verify(passwordEncoder).matches(UPDATE_PASSWORD_DTO_1.getPassword(), ACCOUNT_2.getPassword());
        verify(passwordEncoder).encode(UPDATE_PASSWORD_DTO_1.getNewPassword());
        verify(accountRepository).save(any(Account.class));
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
        assertThrows(BadRequestException.class, ()-> accountService.updatePass(EMAIL_1, UPDATE_PASSWORD_DTO_1));
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
    void updateEmail() {
        // Arrange
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(ACCOUNT_1));
        when(accountMapper.toAccountInfoDTO(ACCOUNT_1)).thenReturn(new AccountInfoDTO());

        // Act
        AccountInfoDTO result = accountService.updateEmail(EMAIL, UPDATE_EMAIL_DTO);

        // Assert
        verify(accountRepository).findByEmail(EMAIL);
        assertEquals(EMAIL_1, ACCOUNT_1.getEmail());
        verify(accountRepository).save(ACCOUNT_1);
        verify(accountMapper).toAccountInfoDTO(ACCOUNT_1);
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
    void getAllTest(){
        // Arrange
        when(accountRepository.findAll()).thenReturn(List.of(ACCOUNT_1));
        when(accountMapper.toAccountInfoDTO(ACCOUNT_1)).thenReturn(ACCOUNT_INFO_DTO);

        // Act
        List<AccountInfoDTO> result = accountService.getAll();

        //Assert
        assertEquals(result, List.of(ACCOUNT_INFO_DTO));
        verify(accountRepository).findAll();
        verify(accountMapper).toAccountInfoDTO(ACCOUNT_1);
    }

    @Test
    void deleteAccountTest(){
        // Arrange
        when(accountRepository.findByUserUserId(USER_ID_1)).thenReturn(Optional.of(ACCOUNT_1));

        // Act
        accountService.deleteAccount(USER_ID_1);

        //Assert
        verify(accountRepository).findByUserUserId(USER_ID_1);
    }

    @Test
    void deleteAccountTest_AccountNotFound(){
        // Arrange
        when(accountRepository.findByUserUserId(USER_ID_1)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () -> accountService.deleteAccount(USER_ID_1));
        verify(accountRepository).findByUserUserId(USER_ID_1);
    }

    @Test
    void getByIdTest(){
        // Arrange
        when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.of(ACCOUNT_1));
        when(accountMapper.toAccountInfoDTO(ACCOUNT_1)).thenReturn(ACCOUNT_INFO_DTO);

        // Act
        AccountInfoDTO result = accountService.getById(ACCOUNT_ID_1);

        //Assert
        assertEquals(result, ACCOUNT_INFO_DTO);
        verify(accountRepository).findById(ACCOUNT_ID_1);
        verify(accountMapper).toAccountInfoDTO(ACCOUNT_1);
    }

    @Test
    void getByIdTest_AccountNotFound() {
        // Arrange
        when(accountRepository.findById(ACCOUNT_ID_1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> accountService.getById(ACCOUNT_ID_1));
        verify(accountRepository).findById(ACCOUNT_ID_1);
    }


}
