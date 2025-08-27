package com.example.demo.Service;

import com.example.demo.Constants.UserConstants;
import com.example.demo.DTO.*;
import com.example.demo.Enum.TaskStatus;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Mapper.TaskMapper;
import com.example.demo.Model.*;
import com.example.demo.Repository.AccountRepository;
import com.example.demo.Repository.ClientRepository;
import com.example.demo.Repository.TaskRepository;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ClientRepository clientRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository,
                       ClientRepository clientRepository,
                       TaskMapper taskMapper,
                       UserRepository userRepository,
                       AccountRepository accountRepository) {
        this.taskRepository = taskRepository;
        this.clientRepository = clientRepository;
        this.taskMapper = taskMapper;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()) {
            throw new NotFoundException("Задача с ID " + id + " не найдена");
        }
        return optionalTask;
    }

    @Transactional
    public Long saveTask(String email, TaskCreateDTO taskCreateDTO, Long inn) {
        User authorAndAssignee = getUserFromAccountEmail(email);
        Client client = clientRepository.findByInn(inn).orElseThrow(() -> new NotFoundException("Клиент с ИНН " + inn + " не найден"));

        Task taskToSave = new Task();
        taskToSave.setTitle(taskCreateDTO.getTitle());
        taskToSave.setDescription(taskCreateDTO.getDescription());
        taskToSave.setClient(client);
        taskToSave.setTaskStatus(TaskStatus.NEW);
        taskToSave.setCreatedAt(LocalDateTime.now());
        taskToSave.setAuthor(authorAndAssignee);
        taskToSave.setAssignee(authorAndAssignee);

        Task savedTask = taskRepository.save(taskToSave);
        return savedTask.getId();
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(taskMapper::toTaskResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasksByClientInn(Long clientInn) {
        Client client = clientRepository.findByInn(clientInn).orElseThrow(
                () -> new NotFoundException("Клиент с ИНН " + clientInn + " не найден"));
        return taskRepository.findByClientInn(client.getInn())
                .stream()
                .map(taskMapper::toTaskResponseDTO)
                .toList();
    }

    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskUpdateDTO taskUpdateDTO) {
        Task taskForUpdate = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Задача с id " + id + " не найдена"));
        taskForUpdate.setTitle(taskUpdateDTO.getTitle());
        taskForUpdate.setDescription(taskUpdateDTO.getDescription());
        taskRepository.save(taskForUpdate);
        return taskMapper.toTaskResponseDTO(taskForUpdate);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getMyTasks(String email) {

        User user = getUserFromAccountEmail(email);

        List<TaskResponseDTO> allUserTasks = taskRepository
                .findAllByUserId(user.getUserId())
                .stream()
                .map(taskMapper::toTaskResponseDTO)
                .collect(Collectors.toList());

        return allUserTasks;
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksCreatedByMe(String email) {

        User user = getUserFromAccountEmail(email);

        List<TaskResponseDTO> myTasks = taskRepository
                .findByAuthorUserId(user.getUserId())
                .stream()
                .map(taskMapper::toTaskResponseDTO)
                .collect(Collectors.toList());

        return myTasks;
    }

    @Transactional
    public TaskResponseDTO changeStatus(String email,
                                        Long taskId,
                                        TaskStatusDTO taskStatusDTO) throws AccessDeniedException {
        User user = getUserFromAccountEmail(email);
        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("Аккаунт с email " + email + " не найден"));

        Task task = taskRepository.findById(taskId).orElseThrow(() ->
                new NotFoundException("Задача не найдена"));

        TaskStatus taskStatus = taskStatusDTO.getNewTaskStatus();

        if (Objects.requireNonNull(taskStatus) == TaskStatus.ARCHIVE) {
            boolean isAuthor = Objects.equals(task.getAuthor().getUserId(), user.getUserId());
            boolean isAdminOrManager = account
                    .getRoles()
                    .stream()
                    .map(Role::getName)
                    .anyMatch(roleName -> "ADMIN".equals(roleName) || "MANAGER".equals(roleName));

            if (!(isAuthor || isAdminOrManager)) {
                throw new AccessDeniedException("У вас недостаточно прав для изменения статуса");
            }
            task.setTaskStatus(TaskStatus.ARCHIVE);
        } else {
            task.setTaskStatus(taskStatus);
        }
        taskRepository.save(task);
        return taskMapper.toTaskResponseDTO(task);
    }

    @Transactional
    public TaskResponseDTO changeAssignee(String email,
                                          Long id,
                                          TaskAssigneeDTO taskAssigneeDTO) throws AccessDeniedException {
        User user = getUserFromAccountEmail(email);
        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("Аккаунт с email " + email + " не найден"));

        Task task = taskRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Задача с ID " + id + " не найдена"));

        User newAssignee = userRepository.findById(taskAssigneeDTO.getNewAssigneeId())
                .orElseThrow(() -> new NotFoundException
                        ("Пользователь с ID " + taskAssigneeDTO.getNewAssigneeId() + " не найден"));

        boolean isAssignee = Objects.equals(task.getAssignee().getUserId(), user.getUserId());
        boolean isAdminOrManager = account
                .getRoles()
                .stream()
                .map(Role::getName)
                .anyMatch(roleName -> "ADMIN".equals(roleName) || "MANAGER".equals(roleName));

        if (!(isAssignee || isAdminOrManager)) {
            throw new AccessDeniedException("У вас нет прав для изменения ответственного сотрудника");
        }
        task.setAssignee(newAssignee);
        taskRepository.save(task);
        return taskMapper.toTaskResponseDTO(task);
    }

    public User getUserFromAccountEmail(String email) {
        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("Аккаунт с email " + email + " не найден"));
        User user = userRepository.findById(account.getUser().getUserId()).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + account.getUser().getUserId() + " не найден"));
        System.out.println("User loaded: " + user.getFirstName() + " " + user.getLastName());

        return user;
    }

    @Transactional
    public void changeAssigneeHandler(Long userId){
        List<Task> usersAssigneeTasks = taskRepository.findAllByUserId(userId);
        User noNameUser = userRepository.findById(UserConstants.NO_NAME_USER_ID).get();
        usersAssigneeTasks.forEach(task -> {
            task.setAssignee(noNameUser);
            taskRepository.save(task);
        });
    }

    @Transactional
    public void changeAuthorHandler(Long userId){
        List<Task> usersCreatedTasks = taskRepository.findByAuthorUserId(userId);
        User noNameUser = userRepository.findById(UserConstants.NO_NAME_USER_ID).get();
        usersCreatedTasks.forEach(task -> {
            task.setAuthor(noNameUser);
            taskRepository.save(task);
        });
    }



}
