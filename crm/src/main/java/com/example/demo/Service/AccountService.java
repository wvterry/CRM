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
import com.example.demo.Model.User;
import com.example.demo.Repository.AccountRepository;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import feign.FeignException;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AccountService {


    private final AccountRepository accountRepository;
    private final UserClient userClient;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AccountMapper accountMapper;
    AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


    @Autowired
    public AccountService(AccountRepository accountRepository,
                          UserClient userClient,
                          UserRepository userRepository,
                          UserMapper userMapper,
                          PasswordEncoder passwordEncoder,
                          RoleRepository roleRepository,
                          AccountMapper accountMapper,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.accountRepository = accountRepository;
        this.userClient = userClient;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.accountMapper = accountMapper;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void register(SignupRequest signupRequest) throws BadRequestException {
        if (accountRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Аккаунт с email " + signupRequest.getEmail() + " уже зарегистрирован");
        }

        Long userId = null;
        try {
            CreateUserResponseDTO createUserRequestDTO = userClient.createUser(userMapper.toCreateUserRequestDTO(signupRequest));
            userId = createUserRequestDTO.getUserId();
        } catch (FeignException.Conflict e) {
            throw new BadRequestException("Пользователь с email " + signupRequest.getEmail() + " уже зарегистрирован");
        }

        User user = userRepository.save(userMapper.toUser(userId, signupRequest));

        String password = passwordEncoder.encode(signupRequest.getPassword());
        Set<Role> roles = new HashSet<>();
        Role accountRole = roleRepository.findByName("USER").orElseThrow(
                () -> new NotFoundException("Роль отсутствует"));
        roles.add(accountRole);

        try {
            accountRepository.save(accountMapper.toAccount(user, userId, roles, password, signupRequest));
        } catch (Exception saveEx) {
            if (userId != null) {
                try {
                    userClient.deleteUser(userId);
                } catch (Exception roleBackEx) {
                    throw new IllegalStateException
                            ("Не удалось удалить пользователя с id " + userId + ". Обратитесь к администратору", roleBackEx);
                }
            }
            throw saveEx;
        }
    }

    @Transactional(readOnly = true)
    public String authenticate(AuthRequest authRequest) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities());
    }

    @Transactional
    public void updatePass(String email, UpdatePasswordDTO updatePasswordDTO) {
        Account account = accountRepository.findByEmail(email).orElseThrow(()
                -> new NotFoundException("Аккаунт с email " + email + " не найден"));

        if (!passwordEncoder.matches(updatePasswordDTO.getPassword(), account.getPassword())) {
            throw new RuntimeException("Вы ввели неверный пароль");
        }
        String password = passwordEncoder.encode(updatePasswordDTO.getNewPassword());
        accountRepository.save(accountMapper.toAccount(password, account));
    }

    @Transactional
    public void updateRole(Long accountId, UpdateAccountRoleDTO updateAccountRoleDTO){
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new NotFoundException("Аккаунт с id " + accountId + " не найден"));

        Role role = roleRepository.findByName(updateAccountRoleDTO.getRole()).orElseThrow(
                () -> new NotFoundException("Роль " + updateAccountRoleDTO.getRole() + " не найдена"));

        Set<Role> roles = new HashSet<>(account.getRoles());
        roles.add(role);
        account.setRoles(roles);

        accountRepository.save(account);
    }

    @Transactional
    public AccountInfoDTO updateEmail(String email, UpdateEmailDTO updateEmailDTO){
        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("Аккаунт с email " + email + " не найден"));

        account.setEmail(updateEmailDTO.getEmail());
        accountRepository.save(account);

        User user = userRepository.findById(account.getUser().getUserId()).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + account.getUser().getUserId() + " не найден"));
        return accountMapper.toAccountInfoDTO(account, user);
    }

    @Transactional(readOnly = true)
    public List<AccountInfoDTO> getAll(){
        return accountRepository.findAll().stream().map(account -> accountMapper.toAccountInfoDTO(account, account.getUser())).toList();
    }

    @Transactional
    public void deleteAccount(Long userId){
        Account account = accountRepository.findByUserId(userId).orElseThrow(
                () -> new NotFoundException ("Аккаунт с пользователем " + userId + " не найден"));
        accountRepository.delete(account);
    }
}

