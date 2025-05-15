package com.example.demo.Mapper;

import com.example.demo.DTO.TaskCreateDTO;
import com.example.demo.DTO.TaskResponseDTO;
import com.example.demo.DTO.TaskUpdateDTO;
import com.example.demo.Model.Client;
import com.example.demo.Model.Task;

public interface TaskMapper {

    public TaskResponseDTO toTaskResponseDTO(Task task);

    public Task toTask(TaskCreateDTO taskCreateDTO, Client client);

    public Task toTaskFromTaskUpdateDTO(TaskUpdateDTO taskUpdateDTO, Task task);
}
