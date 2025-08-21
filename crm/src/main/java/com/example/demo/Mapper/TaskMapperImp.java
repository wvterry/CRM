package com.example.demo.Mapper;

import com.example.demo.DTO.TaskResponseDTO;
import com.example.demo.Model.Task;
import org.springframework.stereotype.Component;

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

}
