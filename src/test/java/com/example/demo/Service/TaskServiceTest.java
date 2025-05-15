package com.example.demo.Service;

import com.example.demo.DTO.TaskCreateDTO;
import com.example.demo.DTO.TaskResponseDTO;
import com.example.demo.DTO.TaskUpdateDTO;
import com.example.demo.Enum.ClientType;
import com.example.demo.Enum.TaskStatus;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Mapper.TaskMapperImp;
import com.example.demo.Model.Client;
import com.example.demo.Model.Task;
import com.example.demo.Repository.ClientRepository;
import com.example.demo.Repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private TaskMapperImp taskMapper;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTaskById_TaskFound(){
        Long id = 1L;
        Task task = new Task();
        task.setId(id);

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.getTaskById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(taskRepository).findById(id);
    }

    @Test
    void testGetTaskById_TaskNotFound_Exception(){
        Long id = 1L;
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.getTaskById(id));
        verify(taskRepository).findById(id);
    }

    @Test
    void testDeleteTaskById_TaskExists_DeletedSuccessfully() {
        Long taskId = 1L;
        Task task = new Task();
        task.setId(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.deleteTaskById(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void testDeleteTaskById_TaskNotFound_ThrowsException() {
        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.deleteTaskById(taskId));
        verify(taskRepository).findById(taskId);
    }

    @Test
    void testGetAllTasksByClientInn(){
        Long inn = 46546L;

        Client client = new Client();
        client.setInn(inn);

        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Test");
        task1.setDescription("test");
        task1.setClient(client);

        List<Task> tasks = List.of(task1);

        TaskResponseDTO taskResponseDTO = new TaskResponseDTO(1L, "Test", "test", inn, TaskStatus.NEW, LocalDateTime.now());

        when(clientRepository.findByInn(inn)).thenReturn(Optional.of(client));
        when(taskRepository.findByClientInn(inn)).thenReturn(tasks);
        when(taskMapper.toTaskResponseDTO(task1)).thenReturn(taskResponseDTO);

        List<TaskResponseDTO> result = taskService.getAllTasksByClientInn(inn);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(taskResponseDTO, result.get(0));
        verify(taskRepository).findByClientInn(inn);
    }

    @Test
    void testGetAllTasks_ReturnsListOfDTOs(){
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");

        List<Task> tasks = List.of(task1, task2);

        TaskResponseDTO dto1 = new TaskResponseDTO(1L, "Task 1", "Desc", 1234567890L, TaskStatus.NEW, LocalDateTime.now());
        TaskResponseDTO dto2 = new TaskResponseDTO(2L, "Task 2", "Desc", 1234567890L, TaskStatus.NEW, LocalDateTime.now());

        when(taskRepository.findAll()).thenReturn(tasks);
        when(taskMapper.toTaskResponseDTO(task1)).thenReturn(dto1);
        when(taskMapper.toTaskResponseDTO(task2)).thenReturn(dto2);

        List<TaskResponseDTO> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));
        verify(taskRepository).findAll();
    }

    @Test
    void testSaveTask_ClientExists_TaskSaved(){
        Long clientInn = 100L;
        Client client = new Client();
        client.setInn(clientInn);

        TaskCreateDTO taskCreateDTO = new TaskCreateDTO("Test", "test");

        Task taskToSave = new Task();
        taskToSave.setId(1L);
        taskToSave.setTitle("Test");
        taskToSave.setDescription("test");
        taskToSave.setClient(client);
        taskToSave.setTaskStatus(TaskStatus.NEW);
        taskToSave.setCreatedAt(LocalDateTime.now());

        when(clientRepository.findByInn(clientInn)).thenReturn(Optional.of(client));
        when(taskMapper.toTask(taskCreateDTO, client)).thenReturn(taskToSave);

        Long savedId = taskService.saveTask(taskCreateDTO, clientInn);

        assertNotNull(savedId);
        assertEquals(1L, savedId);
        verify(taskRepository).save(taskToSave);
        verify(taskMapper).toTask(taskCreateDTO, client);
        verify(clientRepository).findByInn(clientInn);
    }

    @Test
    void testSaveTask_ClientNotFound_ThrowsException(){
        Long clientInn = 1L;
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO("Test", "test");

        when(clientRepository.findByInn(clientInn)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.saveTask(taskCreateDTO, clientInn));
        verify(clientRepository).findByInn(clientInn);

    }

    @Test
    void testGetAllTasksByClientInn_ClientExists(){
        Long inn = 555L;

        Client client = new Client(555L, "Test",
                "88005553535", "test@test.ru", "Test street", ClientType.LEGAL_ENTITY, List.of());

        Task task1 = new Task(1L, "Task 1", "Desc", client, TaskStatus.NEW, LocalDateTime.now());
        Task task2 = new Task(2L, "Task 2", "Desc", client, TaskStatus.NEW, LocalDateTime.now());

        TaskResponseDTO dto1 = new TaskResponseDTO(1L, "Task 1", "Desc", 555L, TaskStatus.NEW, LocalDateTime.now());
        TaskResponseDTO dto2 = new TaskResponseDTO(2L, "Task 2", "Desc", 555L, TaskStatus.NEW, LocalDateTime.now());

        List<TaskResponseDTO> responseDTOS = List.of(dto1, dto2);

        when(clientRepository.findByInn(inn)).thenReturn(Optional.of(client));
        when(taskRepository.findByClientInn(inn)).thenReturn(List.of(task1, task2));
        when(taskMapper.toTaskResponseDTO(task1)).thenReturn(dto1);
        when(taskMapper.toTaskResponseDTO(task2)).thenReturn(dto2);

        List<TaskResponseDTO> result = taskService.getAllTasksByClientInn(inn);

        assertNotNull(result);
        assertEquals(result, responseDTOS);
        verify(clientRepository).findByInn(inn);
        verify(taskRepository).findByClientInn(inn);
        verify(taskMapper).toTaskResponseDTO(task1);
        verify(taskMapper).toTaskResponseDTO(task2);
    }

    @Test
    void testGetAllTasksByClientInn_ClientNotFound_Exception(){
        Long inn = 555L;
        when(clientRepository.findByInn(inn)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.getAllTasksByClientInn(inn));
        verify(clientRepository).findByInn(inn);
    }

    @Test
    void testUpdateTask_TaskExists(){
        Long taskId = 1L;
        Client client = new Client();
        Task task1 = new Task(1L, "Task 1", "Desc", client, TaskStatus.NEW, LocalDateTime.now());
        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO("Task 1", "Desc");
        TaskResponseDTO taskResponseDTO = new TaskResponseDTO(1L, "Task 1", "Desc", 555L, TaskStatus.NEW, LocalDateTime.now());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task1));
        when(taskMapper.toTaskFromTaskUpdateDTO(taskUpdateDTO, task1)).thenReturn(task1);
        when(taskMapper.toTaskResponseDTO(task1)).thenReturn(taskResponseDTO);

        TaskResponseDTO result = taskService.updateTask(taskId, taskUpdateDTO);

        assertNotNull(result);
        assertEquals(result, taskResponseDTO);
        verify(taskRepository).findById(taskId);
        verify(taskMapper).toTaskFromTaskUpdateDTO(taskUpdateDTO, task1);
        verify(taskMapper).toTaskResponseDTO(task1);
    }

    @Test
    void testUpdateTask_TaskNotFound_Exception(){
        Long taskId = 1L;
        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO("Task 1", "Desc");

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, ()-> taskService.updateTask(taskId, taskUpdateDTO));
        verify(taskRepository).findById(taskId);
    }
}
