package com.example.demo.Controller;

import com.example.demo.DTO.TaskResponseDTO;
import com.example.demo.DTO.UpdatePasswordDTO;
import com.example.demo.Service.TaskService;
import com.example.demo.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final TaskService taskService;

    @Autowired
    public UserController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    @PutMapping("/updatepass")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> updatePassword(HttpServletRequest httpServletRequest,
                                               @RequestBody UpdatePasswordDTO updatePasswordDTO){
        userService.updatePass(httpServletRequest, updatePasswordDTO);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/mytasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<List<TaskResponseDTO>> getAllMyTasks(HttpServletRequest httpServletRequest){
        return ResponseEntity.ok(taskService.getMyTasks(httpServletRequest));
    }

    @GetMapping("/mycreatedtasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<List<TaskResponseDTO>> getMyCreaTedtasks(HttpServletRequest httpServletRequest){
        return ResponseEntity.ok(taskService.getOnlyMyTasks(httpServletRequest));
    }

//    @GetMapping("/get/usersbyname")
//    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")

}
