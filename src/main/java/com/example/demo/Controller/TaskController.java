package com.example.demo.Controller;

import com.example.demo.DTO.DTOMapper;
import com.example.demo.DTO.TaskCreateDTO;
import com.example.demo.DTO.TaskResponseDTO;
import com.example.demo.Model.Client;
import com.example.demo.Model.Task;
import com.example.demo.Service.ClientService;
import com.example.demo.Service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController                                                                     //Прочитать про @RestController
@RequestMapping("api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final ClientService clientService;

    @Autowired                                                                      //ПРОЧИТАТЬ ПРО ВНЕДРЕНИЕ ЗАВИСИМОСТЕЙ
    public TaskController(TaskService taskService, ClientService clientService){
        this.taskService = taskService;
        this.clientService = clientService;
    }

    @GetMapping
    public List<TaskResponseDTO> getAllTasks(){
        return taskService.getAllTasks()
                .stream()
                .map(DTOMapper::toTaskResponseDTO)
                .toList();
    }

    @GetMapping("/client/{inn}")
    public List<TaskResponseDTO> getAllTasksByClientInn(@PathVariable Long inn){
        return taskService.getAllTasksByClientInn(inn);
    }

    @PostMapping("/create/{inn}")
    public ResponseEntity<TaskResponseDTO> createTask(@PathVariable Long inn, @RequestBody TaskCreateDTO taskCreateDTO){
        Optional<Client> client = clientService.getClientByInn(inn);
        if (client.isEmpty()){
            throw new RuntimeException("Клиент не найден");
        }
        Task createdTask = taskService.saveTask(taskCreateDTO,client.get());
        return ResponseEntity.ok(DTOMapper.toTaskResponseDTO(createdTask));

    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id){
        taskService.deleteTaskById(id);
    }

}
