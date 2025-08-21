package com.example.demo.Mapper;

import com.example.demo.DTO.TaskResponseDTO;
import com.example.demo.Model.Task;

public interface TaskMapper {

    TaskResponseDTO toTaskResponseDTO(Task task);

}
