package com.example.demo.Controller;

import com.example.demo.DTO.*;
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

    @Autowired
    public TaskController(TaskService taskService){
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(){
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/client/{inn}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<List<TaskResponseDTO>> getAllTasksByClientInn(@PathVariable Long inn){
        return ResponseEntity.ok(taskService.getAllTasksByClientInn(inn));
    }

    @PostMapping("/create/{inn}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<Long> createTask(@PathVariable Long inn, @RequestBody TaskCreateDTO taskCreateDTO){
        Long taskId = taskService.saveTask(taskCreateDTO, inn);
        return ResponseEntity.ok(taskId);
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteTask(@PathVariable Long id){
//          taskService.deleteTaskById(id);
//          return ResponseEntity.noContent().build();
//}

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id,@RequestBody TaskUpdateDTO taskUpdateDTO){
        TaskResponseDTO taskForUpdate = taskService.updateTask(id, taskUpdateDTO);
        return ResponseEntity.ok(taskForUpdate);
    }

    @PutMapping("/change/status/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<TaskResponseDTO> changeStatus(HttpServletRequest httpServletRequest,
                                                        @PathVariable Long id,
                                                        @RequestBody TaskStatusDTO taskStatusDTO) throws AccessDeniedException {
        return ResponseEntity.ok(taskService.changeStatus(httpServletRequest, id, taskStatusDTO));
    }

    @PutMapping("/change/assignee/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<TaskResponseDTO> changeAssignee(HttpServletRequest httpServletRequest,
                                               @PathVariable Long id,
                                               @RequestBody TaskAssigneeDTO taskAssigneeDTO) throws AccessDeniedException {
        return ResponseEntity.ok(taskService.changeAssignee(httpServletRequest, id, taskAssigneeDTO));
    }

}
