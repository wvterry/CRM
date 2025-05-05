package com.example.demo.DTO;

import com.example.demo.Enum.ClientType;
import com.example.demo.Enum.TaskStatus;
import com.example.demo.Model.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientWithTasksDTO {

    private Long inn;

    private String name;

    private ClientType clientType;

    private List<TaskDTO> tasks;

    @Data
    public static class TaskDTO{
        private Long id;
        private String title;
        private String description;
        private TaskStatus taskStatus;
        private LocalDateTime createdAt;
    }
}
