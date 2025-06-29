package com.example.demo.DTO;

import com.example.demo.Enum.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskStatusDTO {

    private TaskStatus newTaskStatus;
}
