package com.example.demo.Service;

import com.example.demo.DTO.*;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.JWT.AuthRequest;
import com.example.demo.JWT.JwtUtil;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.Role;
import com.example.demo.Model.User;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtUtil jwtUtil,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public String authenticate(AuthRequest authRequest){
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities());
    }

    @Transactional
    public void register(SignupRequest signupRequest) throws BadRequestException {
        if (userRepository.existsByEmail(signupRequest.getEmail())){
            throw new BadRequestException("Пользователь с email " + signupRequest.getEmail() + " уже зарегистрирован");
        }
        String password = passwordEncoder.encode(signupRequest.getPassword());
        Set<Role> roles = new HashSet<>();
        Optional<Role> userRole = roleRepository.findByName("USER");
        roles.add(userRole.get());

        userRepository.save(userMapper.toUser(roles, password, signupRequest));
        }

    @Transactional
    public void updatePass(String email, UpdatePasswordDTO updatePasswordDTO){
        User user = userRepository.findUserByEmail(email).orElseThrow(() ->
                new NotFoundException("Пользователь с email " + email + " не найден"));

        if (!passwordEncoder.matches(updatePasswordDTO.getPassword(), user.getPassword())){
            throw new RuntimeException("Вы ввели неверный пароль");
        }
        String password = passwordEncoder.encode(updatePasswordDTO.getNewPassword());
        userRepository.save(userMapper.toUser(password, user, updatePasswordDTO));
    }

    @Transactional
    public UserInfoDTO updateUser(Long userId, UpdateUserDTO updateUserDTO){

        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с ID " + userId + " не найден"));

        String password = passwordEncoder.encode(user.getPassword());

        userRepository.save(userMapper.toUser(password, user, updateUserDTO));
        return userMapper.toUserInfoDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserInfoDTO> getAllUsersInfo(){
        return userRepository
                .findAll()
                .stream()
                .map(userMapper::toUserInfoDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserRole(Long id, UpdateUserRoleDTO updateUserRoleDTO) {
        User user = userRepository.findById(id).orElseThrow(()->
                new NotFoundException("Пользователь с ID" + id + " не найден"));

        Role newRole = roleRepository.findByName(updateUserRoleDTO.getRole())
                .orElseThrow(() -> new NotFoundException("Роль не найдена"));
        Set<Role> roles = new HashSet<>(user.getRoles());
        if (!roles.contains(newRole)) {
            roles.add(newRole);

            user.setRoles(roles);
        }
        userRepository.save(user);
    }

    }

