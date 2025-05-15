package com.example.demo.Controller;

import com.example.demo.DTO.TaskCreateDTO;
import com.example.demo.DTO.TaskResponseDTO;
import com.example.demo.DTO.TaskUpdateDTO;
import com.example.demo.Service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<TaskResponseDTO>> getAllTasksByClientInn(@PathVariable Long inn){
        return ResponseEntity.ok(taskService.getAllTasksByClientInn(inn));
    }

    @PostMapping("/create/{inn}")
    public ResponseEntity<Long> createTask(@PathVariable Long inn, @RequestBody TaskCreateDTO taskCreateDTO){
        Long taskId = taskService.saveTask(taskCreateDTO, inn);
        return ResponseEntity.ok(taskId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id){
          taskService.deleteTaskById(id);
          return ResponseEntity.noContent().build();
}


    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id,@RequestBody TaskUpdateDTO taskUpdateDTO){
        TaskResponseDTO taskForUpdate = taskService.updateTask(id, taskUpdateDTO);
        return ResponseEntity.ok(taskForUpdate);
    }
}
