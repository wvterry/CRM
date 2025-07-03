package com.example.demo.Controller;

import com.example.demo.DTO.*;
import com.example.demo.JWT.JwtUtil;
import com.example.demo.Model.User;
import com.example.demo.Service.TaskService;
import com.example.demo.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final TaskService taskService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, TaskService taskService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.taskService = taskService;
        this.jwtUtil = jwtUtil;
    }

    @PutMapping("/updatepass")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> updatePassword(HttpServletRequest httpServletRequest,
                                               @RequestBody UpdatePasswordDTO updatePasswordDTO){
        String token = jwtUtil.getTokenFromRequest(httpServletRequest);
        String email = jwtUtil.getEmailFromToken(token);
        userService.updatePass(email, updatePasswordDTO);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfoDTO> updateUser(@PathVariable Long id,
                                                     @RequestBody UpdateUserDTO updateUserDTO){
        return ResponseEntity.ok(userService.updateUser(id, updateUserDTO));
    }

    @GetMapping("/get/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserInfoDTO>> getAllUserInfo(){
        return ResponseEntity.ok(userService.getAllUsersInfo());
    }

    @PutMapping("/update/userrole/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id,@RequestBody UpdateUserRoleDTO updateUserRoleDTO){
        userService.updateUserRole(id, updateUserRoleDTO);
        return ResponseEntity.ok().build();
    }



}
