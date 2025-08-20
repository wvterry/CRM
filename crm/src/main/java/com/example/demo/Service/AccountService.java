package com.example.demo.Service;

import com.example.demo.DTO.*;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Feign.UserClient;
import com.example.demo.JWT.AuthRequest;
import com.example.demo.JWT.CustomUserDetailsService;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserClient userClient;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AccountMapper accountMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public AccountService(AccountRepository accountRepository,
                          UserClient userClient,
                          UserRepository userRepository,
                          UserMapper userMapper,
                          PasswordEncoder passwordEncoder,
                          RoleRepository roleRepository,
                          AccountMapper accountMapper,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserService userService,
                          TransactionTemplate transactionTemplate) {
        this.accountRepository = accountRepository;
        this.userClient = userClient;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.accountMapper = accountMapper;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.transactionTemplate = transactionTemplate;
    }

    @Transactional
    public void register(SignupRequest signupRequest) throws BadRequestException {
        if (accountRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Аккаунт с email " + signupRequest.getEmail() + " уже зарегистрирован");
        }

        User user = userService.saveUser(signupRequest);
        String password = passwordEncoder.encode(signupRequest.getPassword());
        Set<Role> roles = new HashSet<>();
        Role accountRole = roleRepository.findByName("USER").orElseThrow(
                () -> new NotFoundException("Роль отсутствует"));
        roles.add(accountRole);

        Account accountToSave = new Account();
        accountToSave.setEmail(signupRequest.getEmail());
        accountToSave.setPassword(password);
        accountToSave.setRoles(roles);
        accountToSave.setUser(user);
        accountToSave.setCreatedAt(LocalDateTime.now());

        try {
            transactionTemplate.executeWithoutResult(transactionStatus -> {accountRepository.save(accountToSave);});
        } catch (Exception saveEx) {
            if (user.getUserId() != null) {
                try {
                    userClient.deleteUser(user.getUserId());
                } catch (Exception roleBackEx) {
                    throw new IllegalStateException
                            ("Не удалось удалить пользователя с id " + user.getUserId() +
                                    ". Обратитесь к администратору", roleBackEx);
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
    public void updatePass(String email, UpdatePasswordDTO updatePasswordDTO) throws BadRequestException {
        Account account = accountRepository.findByEmail(email).orElseThrow(()
                -> new NotFoundException("Аккаунт с email " + email + " не найден"));

        if (!passwordEncoder.matches(updatePasswordDTO.getPassword(), account.getPassword())) {
            throw new BadRequestException("Вы ввели неверный пароль");
        }
        String password = passwordEncoder.encode(updatePasswordDTO.getNewPassword());
        account.setPassword(password);
        accountRepository.save(account);
    }

    @Transactional
    public void updateRole(Long accountId, UpdateAccountRoleDTO updateAccountRoleDTO) {
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
    public AccountInfoDTO updateEmail(String email, UpdateEmailDTO updateEmailDTO) {
        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("Аккаунт с email " + email + " не найден"));

        account.setEmail(updateEmailDTO.getEmail());
        accountRepository.save(account);
        return accountMapper.toAccountInfoDTO(account);
    }

    @Transactional(readOnly = true)
    public List<AccountInfoDTO> getAll() {
        return accountRepository.findAll().stream().map(accountMapper::toAccountInfoDTO).toList();
    }

    @Transactional
    public void deleteAccount(Long userId) {
        Account account = accountRepository.findByUserUserId(userId).orElseThrow(
                () -> new NotFoundException("Аккаунт с пользователем " + userId + " не найден"));
        accountRepository.delete(account);
    }
}

