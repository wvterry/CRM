package com.example.demo.Service;

import com.example.demo.DTO.TaskCreateDTO;
import com.example.demo.DTO.TaskResponseDTO;
import com.example.demo.DTO.TaskUpdateDTO;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Mapper.TaskMapper;
import com.example.demo.Mapper.TaskMapperImp;
import com.example.demo.Model.Client;
import com.example.demo.Model.Task;
import com.example.demo.Repository.ClientRepository;
import com.example.demo.Repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ClientRepository clientRepository;
    private final TaskMapper taskMapper;


    @Autowired
    public TaskService (TaskRepository taskRepository, ClientRepository clientRepository, TaskMapper taskMapper){
        this.taskRepository = taskRepository;
        this.clientRepository = clientRepository;
        this.taskMapper = taskMapper;
    }

    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id){
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()){
            throw new NotFoundException("Задача с ID " + id + " не найдена");
        }
        return optionalTask;
    }

    @Transactional
    public Long saveTask(TaskCreateDTO taskCreateDTO, Long inn){
        Client client = clientRepository.findByInn(inn).orElseThrow(() -> new NotFoundException("Клиент с ИНН " + inn + " не найден"));
        Task taskToSave = taskMapper.toTask(taskCreateDTO, client);
        taskRepository.save(taskToSave);
        return taskToSave.getId();
    }

    @Transactional
    public void deleteTaskById(Long id){
        Optional<Task> taskForDelete = taskRepository.findById(id);
        if (taskForDelete.isEmpty()){
            throw new NotFoundException("Задача с ID " + id + " не найдена");
        }
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks(){
        return taskRepository.findAll()
                .stream()
                .map(taskMapper::toTaskResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasksByClientInn(Long clientInn){
        Client client = clientRepository.findByInn(clientInn).orElseThrow(() -> new NotFoundException("Клиент с ИНН " + clientInn + " не найден"));
        return taskRepository.findByClientInn(client.getInn())
                .stream()
                .map(taskMapper::toTaskResponseDTO)
                .toList();
    }

    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskUpdateDTO taskUpdateDTO){
        Optional<Task> taskForUpdate = taskRepository.findById(id);
        if (taskForUpdate.isEmpty()){
            throw new NotFoundException("Задача с ID " + id + " не найдена");
        }
        Task updatedTask = taskMapper.toTaskFromTaskUpdateDTO(taskUpdateDTO, taskForUpdate.get());
        taskRepository.save(updatedTask);
        return taskMapper.toTaskResponseDTO(updatedTask);
    }

}
