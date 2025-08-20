package com.example.demo.Mapper;

import com.example.demo.DTO.TaskCreateDTO;
import com.example.demo.DTO.TaskResponseDTO;
import com.example.demo.DTO.TaskUpdateDTO;
import com.example.demo.Model.Client;
import com.example.demo.Model.Task;
import com.example.demo.Model.User;

public interface TaskMapper {

    public TaskResponseDTO toTaskResponseDTO(Task task);

}
