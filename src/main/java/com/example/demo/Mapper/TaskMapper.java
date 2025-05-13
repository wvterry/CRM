package com.example.demo.Mapper;

import com.example.demo.DTO.TaskCreateDTO;
import com.example.demo.DTO.TaskResponseDTO;
import com.example.demo.Enum.TaskStatus;
import com.example.demo.Model.Client;
import com.example.demo.Model.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TaskMapper {

    public TaskResponseDTO toTaskResponseDTO(Task task){
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getClient().getInn(),
                task.getTaskStatus(),
                task.getCreatedAt()
        );
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
}
