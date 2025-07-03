package com.example.demo.Controller;

import com.example.demo.DTO.*;
import com.example.demo.JWT.JwtUtil;
import com.example.demo.Service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("api/tasks")
public class TaskController {

    private final TaskService taskService;

    private final JwtUtil jwtUtil;

    @Autowired
    public TaskController(TaskService taskService, JwtUtil jwtUtil){
        this.jwtUtil = jwtUtil;
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(){
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/client/{inn}")
    public ResponseEntity<List<TaskResponseDTO>> getAllTasksByClientInn(@PathVariable Long inn){
        return ResponseEntity.ok(taskService.getAllTasksByClientInn(inn));
    }

    @PostMapping("/create/{inn}")
    public ResponseEntity<Long> createTask(@PathVariable Long inn, @RequestBody TaskCreateDTO taskCreateDTO){
        Long taskId = taskService.saveTask(taskCreateDTO, inn);
        return ResponseEntity.ok(taskId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id,@RequestBody TaskUpdateDTO taskUpdateDTO){
        TaskResponseDTO taskForUpdate = taskService.updateTask(id, taskUpdateDTO);
        return ResponseEntity.ok(taskForUpdate);
    }

    @PutMapping("/change/status/{id}")
    public ResponseEntity<TaskResponseDTO> changeStatus(HttpServletRequest httpServletRequest,
                                                        @PathVariable Long id,
                                                        @RequestBody TaskStatusDTO taskStatusDTO) throws AccessDeniedException {
        String token = jwtUtil.getTokenFromRequest(httpServletRequest);
        String email = jwtUtil.getEmailFromToken(token);
        return ResponseEntity.ok(taskService.changeStatus(email, id, taskStatusDTO));
    }

    @PutMapping("/change/assignee/{id}")
    public ResponseEntity<TaskResponseDTO> changeAssignee(HttpServletRequest httpServletRequest,
                                               @PathVariable Long id,
                                               @RequestBody TaskAssigneeDTO taskAssigneeDTO) throws AccessDeniedException {
        String token = jwtUtil.getTokenFromRequest(httpServletRequest);
        String email = jwtUtil.getEmailFromToken(token);
        return ResponseEntity.ok(taskService.changeAssignee(email, id, taskAssigneeDTO));
    }

    @GetMapping("/mytasks")
    public ResponseEntity<List<TaskResponseDTO>> getAllMyTasks(HttpServletRequest httpServletRequest){
        String token = jwtUtil.getTokenFromRequest(httpServletRequest);
        String email = jwtUtil.getEmailFromToken(token);
        return ResponseEntity.ok(taskService.getMyTasks(email));
    }

    @GetMapping("/mycreatedtasks")
    public ResponseEntity<List<TaskResponseDTO>> getTasksCreatedByCurrentUser(HttpServletRequest httpServletRequest){
        String token = jwtUtil.getTokenFromRequest(httpServletRequest);
        String email = jwtUtil.getEmailFromToken(token);
        return ResponseEntity.ok(taskService.getTasksCreatedByMe(email));
    }

}
