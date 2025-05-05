package com.example.demo.DTO;

import com.example.demo.Enum.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskResponseDTO {

    private Long id;

    private String title;

    private String description;

    private Long clientInn;

    private TaskStatus taskStatus;

    private LocalDateTime createdAt;

}
