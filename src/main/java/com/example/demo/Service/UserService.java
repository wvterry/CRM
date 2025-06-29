package com.example.demo.Service;

import com.example.demo.DTO.UpdatePasswordDTO;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Exception.RegisteredException;
import com.example.demo.JWT.AuthRequest;
import com.example.demo.JWT.JwtUtil;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtUtil jwtUtil,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public String authenticate(AuthRequest authRequest){
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities());
    }


    public void register(SignupRequest signupRequest){
        if (userRepository.existsByEmail(signupRequest.getEmail())){
            throw new RegisteredException("Пользователь с email " + signupRequest.getEmail() + " уже зарегистрирован");
        }
        userRepository.save(userMapper.toUserFromSignupRequest(signupRequest));
        }


    public void updatePass(HttpServletRequest httpServletRequest, UpdatePasswordDTO updatePasswordDTO){
        String token = jwtUtil.getTokenFromRequest(httpServletRequest);
        Optional<User> optionalUser = userRepository.findUserByEmail(jwtUtil.getEmailFromToken(token));

        if (!passwordEncoder.matches(updatePasswordDTO.getPassword(), optionalUser.get().getPassword())){
            throw new RuntimeException("Вы ввели неверный пароль");
        }
        userRepository.save(userMapper.toUserFromUpdatePasswordDTO(optionalUser.get(), updatePasswordDTO));
    }

    }

