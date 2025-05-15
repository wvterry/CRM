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

    private static final Long CLIENT_INN_555 = 555L;
    private static final Long TASK_ID_1 = 1L;
    private static final Long TASK_ID_2 = 2L;

    private static final Client CLIENT_555 = new Client(
            CLIENT_INN_555,
            "Test Company",
            "88005553535",
            "test@test.ru",
            "Test street",
            ClientType.LEGAL_ENTITY,
            List.of()
    );

    private static final Task TASK_1 = new Task(
            TASK_ID_1,
            "Task 1",
            "Description 1",
            CLIENT_555,
            TaskStatus.NEW,
            LocalDateTime.now()
    );

    private static final Task TASK_2 = new Task(
            TASK_ID_2,
            "Task 2",
            "Description 2",
            CLIENT_555,
            TaskStatus.IN_PROGRESS,
            LocalDateTime.now()
    );

    private static final TaskResponseDTO TASK_RESPONSE_DTO_1 = new TaskResponseDTO(
            TASK_ID_1,
            "Task 1",
            "Description 1",
            CLIENT_INN_555,
            TaskStatus.NEW,
            LocalDateTime.now()
    );

    private static final TaskResponseDTO TASK_RESPONSE_DTO_2 = new TaskResponseDTO(
            TASK_ID_2,
            "Task 2",
            "Description 2",
            CLIENT_INN_555,
            TaskStatus.IN_PROGRESS,
            LocalDateTime.now()
    );

    private static final TaskCreateDTO TASK_CREATE_DTO = new TaskCreateDTO("New Task", "New Description");
    private static final TaskUpdateDTO TASK_UPDATE_DTO = new TaskUpdateDTO("Updated Title", "Updated Description");

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
        // Arrange
        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(TASK_1));

        // Act
        Optional<Task> result = taskService.getTaskById(TASK_ID_1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TASK_ID_1, result.get().getId());
        verify(taskRepository).findById(TASK_ID_1);
    }

    @Test
    void testGetTaskById_TaskNotFound_Exception(){
        // Arrange
        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> taskService.getTaskById(TASK_ID_1));
        verify(taskRepository).findById(TASK_ID_1);
    }

    @Test
    void testDeleteTaskById_TaskExists_DeletedSuccessfully() {
        // Arrange
        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(TASK_1));

        // Act
        taskService.deleteTaskById(TASK_ID_1);

        // Assert
        verify(taskRepository).findById(TASK_ID_1);
        verify(taskRepository).deleteById(TASK_ID_1);
    }

    @Test
    void testDeleteTaskById_TaskNotFound_ThrowsException() {
        // Arrange
        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> taskService.deleteTaskById(TASK_ID_1));
        verify(taskRepository).findById(TASK_ID_1);
    }

    @Test
    void testGetAllTasksByClientInn(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_555)).thenReturn(Optional.of(CLIENT_555));
        when(taskRepository.findByClientInn(CLIENT_INN_555)).thenReturn(List.of(TASK_1));
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);

        // Act
        List<TaskResponseDTO> result = taskService.getAllTasksByClientInn(CLIENT_INN_555);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TASK_RESPONSE_DTO_1, result.get(0));
        verify(taskRepository).findByClientInn(CLIENT_INN_555);
    }

    @Test
    void testGetAllTasks_ReturnsListOfDTOs(){
        // Arrange
        when(taskRepository.findAll()).thenReturn(List.of(TASK_1, TASK_2));
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);
        when(taskMapper.toTaskResponseDTO(TASK_2)).thenReturn(TASK_RESPONSE_DTO_2);

        // Act
        List<TaskResponseDTO> result = taskService.getAllTasks();

        // Assert
        assertEquals(2, result.size());
        assertEquals(TASK_RESPONSE_DTO_1, result.get(0));
        assertEquals(TASK_RESPONSE_DTO_2, result.get(1));
        verify(taskRepository).findAll();
    }

    @Test
    void testSaveTask_ClientExists_TaskSaved(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_555)).thenReturn(Optional.of(CLIENT_555));
        when(taskMapper.toTask(TASK_CREATE_DTO, CLIENT_555)).thenReturn(TASK_1);

        // Act
        Long savedId = taskService.saveTask(TASK_CREATE_DTO, CLIENT_INN_555);

        // Assert
        assertNotNull(savedId);
        assertEquals(1L, savedId);
        verify(taskRepository).save(TASK_1);
        verify(taskMapper).toTask(TASK_CREATE_DTO, CLIENT_555);
        verify(clientRepository).findByInn(CLIENT_INN_555);
    }

    @Test
    void testSaveTask_ClientNotFound_ThrowsException(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_555)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> taskService.saveTask(TASK_CREATE_DTO, CLIENT_INN_555));
        verify(clientRepository).findByInn(CLIENT_INN_555);
    }

    @Test
    void testGetAllTasksByClientInn_ClientExists(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_555)).thenReturn(Optional.of(CLIENT_555));
        when(taskRepository.findByClientInn(CLIENT_INN_555)).thenReturn(List.of(TASK_1, TASK_2));
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);
        when(taskMapper.toTaskResponseDTO(TASK_2)).thenReturn(TASK_RESPONSE_DTO_2);

        // Act
        List<TaskResponseDTO> result = taskService.getAllTasksByClientInn(CLIENT_INN_555);

        // Assert
        assertNotNull(result);
        assertEquals(result, List.of(TASK_RESPONSE_DTO_1, TASK_RESPONSE_DTO_2));
        verify(clientRepository).findByInn(CLIENT_INN_555);
        verify(taskRepository).findByClientInn(CLIENT_INN_555);
        verify(taskMapper).toTaskResponseDTO(TASK_1);
        verify(taskMapper).toTaskResponseDTO(TASK_2);
    }

    @Test
    void testGetAllTasksByClientInn_ClientNotFound_Exception(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_555)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> taskService.getAllTasksByClientInn(CLIENT_INN_555));
        verify(clientRepository).findByInn(CLIENT_INN_555);
    }

    @Test
    void testUpdateTask_TaskExists(){
        // Arrange
        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(TASK_1));
        when(taskMapper.toTaskFromTaskUpdateDTO(TASK_UPDATE_DTO, TASK_1)).thenReturn(TASK_1);
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);

        // Act
        TaskResponseDTO result = taskService.updateTask(TASK_ID_1, TASK_UPDATE_DTO);

        // Assert
        assertNotNull(result);
        assertEquals(result, TASK_RESPONSE_DTO_1);
        verify(taskRepository).findById(TASK_ID_1);
        verify(taskMapper).toTaskFromTaskUpdateDTO(TASK_UPDATE_DTO, TASK_1);
        verify(taskMapper).toTaskResponseDTO(TASK_1);
    }

    @Test
    void testUpdateTask_TaskNotFound_Exception(){
        // Arrange
        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, ()-> taskService.updateTask(TASK_ID_1, TASK_UPDATE_DTO));
        verify(taskRepository).findById(TASK_ID_1);
    }
}
