package com.example.demo.Service;

import com.example.demo.DTO.AccountInfoDTO;
import com.example.demo.DTO.UpdateAccountRoleDTO;
import com.example.demo.DTO.UpdateEmailDTO;
import com.example.demo.DTO.UpdatePasswordDTO;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Feign.UserClient;
import com.example.demo.JWT.AuthRequest;
import com.example.demo.JWT.JwtTokenService;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Mapper.AccountMapper;
import com.example.demo.Model.Account;
import com.example.demo.Model.Role;
import com.example.demo.Model.User;
import com.example.demo.Repository.AccountRepository;
import com.example.demo.Repository.RoleRepository;
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
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AccountMapper accountMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtUtil;
    private final UserService userService;
    private final TaskService taskService;
    private final ClientService clientService;

    @Autowired
    public AccountService(AccountRepository accountRepository,
                          UserClient userClient,
                          PasswordEncoder passwordEncoder,
                          RoleRepository roleRepository,
                          AccountMapper accountMapper,
                          AuthenticationManager authenticationManager,
                          JwtTokenService jwtUtil,
                          UserService userService,
                          TaskService taskService,
                          ClientService clientService) {
        this.accountRepository = accountRepository;
        this.userClient = userClient;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.accountMapper = accountMapper;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.taskService = taskService;
        this.clientService = clientService;
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
            accountRepository.save(accountToSave);
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        log.info("Аккаунт успешно зарегистрирован: {}", accountToSave.getEmail());
                    }
                });
            } else {
                log.debug("Транзакция не активна — afterCommit обработчик не зарегистрирован");
            }
        } catch (Exception saveEx) {
            if (user.getUserId() != null) {
                try {
                    userClient.deleteUser(user.getUserId());
                } catch (Exception roleBackEx) {
                    throw new IllegalStateException(
                            "Не удалось удалить пользователя с id " + user.getUserId() +
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

        taskService.changeAssigneeHandler(userId);
        taskService.changeAuthorHandler(userId);
        clientService.changeManagerHandler(userId);

        accountRepository.delete(account);
    }

    @Transactional(readOnly = true)
    public AccountInfoDTO getById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Аккаунт с id " + id + " не найден"));
        return accountMapper.toAccountInfoDTO(account);
    }
}

