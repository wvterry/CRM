package com.example.demo.Mapper;

import com.example.demo.DTO.TaskCreateDTO;
import com.example.demo.DTO.TaskResponseDTO;
import com.example.demo.DTO.TaskUpdateDTO;
import com.example.demo.Enum.TaskStatus;
import com.example.demo.Model.Client;
import com.example.demo.Model.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaskMapperImp implements TaskMapper {

        public TaskResponseDTO toTaskResponseDTO(Task task){
            TaskResponseDTO taskResponseDTO = new TaskResponseDTO();
            taskResponseDTO.setId(task.getId());
            taskResponseDTO.setTitle(task.getTitle());
            taskResponseDTO.setDescription(task.getDescription());
            taskResponseDTO.setClientInn(task.getClient().getInn());
            taskResponseDTO.setTaskStatus(task.getTaskStatus());
            taskResponseDTO.setCreatedAt(task.getCreatedAt());
            taskResponseDTO.setAssignee(task.getAssignee().getFirstName() + " " + task.getAssignee().getLastName());
            return taskResponseDTO;
    }

    public Task toTask(TaskCreateDTO taskCreateDTO, Client client){
        Task task = new Task();
        task.setTitle(taskCreateDTO.getTitle());
        task.setDescription(taskCreateDTO.getDescription());
        task.setClient(client);
        task.setTaskStatus(TaskStatus.NEW);
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }

    public Task toTask(TaskUpdateDTO taskUpdateDTO, Task task){
        Task toTaskFromTaskUpdate = new Task();
        toTaskFromTaskUpdate.setId(task.getId());
        toTaskFromTaskUpdate.setTaskStatus(task.getTaskStatus());
        toTaskFromTaskUpdate.setCreatedAt(task.getCreatedAt());
        toTaskFromTaskUpdate.setClient(task.getClient());
        toTaskFromTaskUpdate.setTitle(taskUpdateDTO.getTitle());
        toTaskFromTaskUpdate.setDescription(taskUpdateDTO.getDescription());
        return toTaskFromTaskUpdate;
    }
}
