package com.example.demo.Service;

import com.example.demo.DTO.*;
import com.example.demo.Enum.TaskStatus;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.JWT.JwtUtil;
import com.example.demo.Mapper.TaskMapper;
import com.example.demo.Mapper.TaskMapperImp;
import com.example.demo.Model.Client;
import com.example.demo.Model.Role;
import com.example.demo.Model.Task;
import com.example.demo.Model.User;
import com.example.demo.Repository.ClientRepository;
import com.example.demo.Repository.TaskRepository;
import com.example.demo.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ClientRepository clientRepository;
    private final TaskMapper taskMapper;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;


    @Autowired
    public TaskService (TaskRepository taskRepository,
                        ClientRepository clientRepository,
                        TaskMapper taskMapper,
                        JwtUtil jwtUtil,
                        UserRepository userRepository){
        this.taskRepository = taskRepository;
        this.clientRepository = clientRepository;
        this.taskMapper = taskMapper;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
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

//    @Transactional
//    public void deleteTaskById(Long id){
//        Optional<Task> taskForDelete = taskRepository.findById(id);
//        if (taskForDelete.isEmpty()){
//            throw new NotFoundException("Задача с ID " + id + " не найдена");
//        }
//        taskRepository.deleteById(id);
//    }

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

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getMyTasks(HttpServletRequest httpServletRequest){
        String token = jwtUtil.getTokenFromRequest(httpServletRequest);
        String email = jwtUtil.getEmailFromToken(token);
        Optional<User> user = userRepository.findUserByEmail(email);
        if (user.isEmpty()){
            throw new NotFoundException("Пользователь с email " + email + " не найден");
        }
        List<TaskResponseDTO> allUserTasks = taskRepository
                .findAllByUserId(user.get().getUserId())
                .stream()
                .map(taskMapper::toTaskResponseDTO)
                .collect(Collectors.toList());

        return allUserTasks;
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getOnlyMyTasks(HttpServletRequest httpServletRequest){
        String token = jwtUtil.getTokenFromRequest(httpServletRequest);
        String email = jwtUtil.getEmailFromToken(token);
        Optional<User> user = userRepository.findUserByEmail(email);
        if (user.isEmpty()){
            throw new NotFoundException("Пользователь с email " + email + " не найден");
        }
        List<TaskResponseDTO> myTasks = taskRepository
                .findAllMyCreatedTasksByUserId(user.get().getUserId())
                .stream()
                .map(taskMapper::toTaskResponseDTO)
                .collect(Collectors.toList());

        return myTasks;
    }

    @Transactional
    public TaskResponseDTO changeStatus(HttpServletRequest httpServletRequest,
                                        Long taskId,
                                        TaskStatusDTO taskStatusDTO) throws AccessDeniedException {
        // тут я найду юзера
        String token = jwtUtil.getTokenFromRequest(httpServletRequest);
        String email = jwtUtil.getEmailFromToken(token);
        Optional<User> user = userRepository.findUserByEmail(email);
        if (user.isEmpty()){
            throw new NotFoundException("Пользователь с email " + email + " не найден");
        }

        //тут я найду таску
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Задача не найдена"));

        //тут я узнаю новый статус
        TaskStatus taskStatus = taskStatusDTO.getNewTaskStatus();

        //тут будет основная логика
        switch (taskStatus) {
            case ARCHIVE -> {
                boolean isAuthor = task.getAuthor().getUserId() == user.get().getUserId();
                boolean isAdminOrManager = user.get()
                        .getRoles()
                        .stream()
                        .map(Role::getName)
                        .anyMatch(roleName -> "ADMIN".equals(roleName) || "MANAGER".equals(roleName));

                if (!(isAuthor || isAdminOrManager)){
                    throw new AccessDeniedException("У вас недостаточно прав для изменения статуса");
                }
                task.setTaskStatus(TaskStatus.ARCHIVE);
            }
            default -> {
                task.setTaskStatus(taskStatus);
            }
        }
        return taskMapper.toTaskResponseDTO(task);
    }

    @Transactional
    public TaskResponseDTO changeAssignee (HttpServletRequest httpServletRequest,
                                           Long id,
                                           TaskAssigneeDTO taskAssigneeDTO) throws AccessDeniedException {
        String token = jwtUtil.getTokenFromRequest(httpServletRequest);
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Задача с ID " + id + " не найдена"));
        User newAssignee = userRepository.findById(taskAssigneeDTO.getNewAssigneeId())
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + taskAssigneeDTO.getNewAssigneeId() + " не найден"));

        boolean isAssignee = task.getAssignee().getUserId() == user.getUserId();
        boolean isAdminOrManager = user
                .getRoles()
                .stream()
                .map(Role::getName)
                .anyMatch(roleName -> "ADMIN".equals(roleName) || "MANAGER".equals(roleName));

        if (!(isAssignee || isAdminOrManager)){
            throw new AccessDeniedException("У вас нет прав для изменения ответственного сотрудника");
        }
        task.setAssignee(newAssignee);
        return taskMapper.toTaskResponseDTO(task);
    }


}
