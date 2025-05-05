package com.example.demo.Service;

import com.example.demo.DTO.ClientWithTasksDTO;
import com.example.demo.DTO.DTOMapper;
import com.example.demo.DTO.TaskCreateDTO;
import com.example.demo.DTO.TaskResponseDTO;
import com.example.demo.Model.Client;
import com.example.demo.Model.Task;
import com.example.demo.Repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;


    @Autowired
    public TaskService (TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Task saveTask(TaskCreateDTO taskCreateDTO, Client client){
        Task taskToSave = DTOMapper.toTask(taskCreateDTO, client);
        return taskRepository.save(taskToSave);
    }

    @Transactional
    public void deleteTaskById(Long id){
        taskRepository.deleteById(id);
    }

    @Transactional
    public List<Task> getAllTasks(){
        return taskRepository.findAll();
    }

    @Transactional
    public List<TaskResponseDTO> getAllTasksByClientInn(Long clientInn){
        return taskRepository.findByClientInn(clientInn)
                .stream()
                .map(DTOMapper::toTaskResponseDTO)
                .toList();
    }

}
