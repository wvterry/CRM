package com.example.demo.Service;

import com.example.demo.DTO.*;
import com.example.demo.Enum.ClientType;
import com.example.demo.Enum.TaskStatus;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Mapper.TaskMapperImp;
import com.example.demo.Model.*;
import com.example.demo.Repository.AccountRepository;
import com.example.demo.Repository.ClientRepository;
import com.example.demo.Repository.TaskRepository;
import com.example.demo.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskServiceTest {

    private static final Role USER_ROLE = new Role("USER");
    private static final Role ADMIN_ROLE = new Role("ADMIN");
    private static final Long USER_ID_1 = 1L;
    private static final Long USER_ID_2 = 2L;
    private static final User USER_1 = new User(USER_ID_1, "Ivan", "Ivanov");
    private static final User USER_2 = new User(USER_ID_2,"Egor", "Egorov");
    private static final Long CLIENT_INN_1 = 1L;
    private static final Long CLIENT_INN_2 = 2L;
    private static final Long TASK_ID_1 = 1L;
    private static final Long TASK_ID_2 = 2L;
    private static final Long TASK_ID_3 = 3L;
    private static final String EMAIL_1 = "ivan@mail.com";
    private static final String EMAIL_2 = "egor@mail.com";

    private static final Account ACCOUNT_1 = new Account(1L,
            "ivan@mail.com",
            USER_1,
            Set.of(USER_ROLE));

    private static final Account ACCOUNT_2 = new Account(2L,
            "egor@mail.com",
            USER_2,
            Set.of(ADMIN_ROLE));
    private static final TaskResponseDTO TASK_RESPONSE_DTO_1 = new TaskResponseDTO(TASK_ID_1,
            "Task 1",
            "Description 1",
            CLIENT_INN_1,
            TaskStatus.NEW,
            LocalDateTime.now(),
            "Test Name");

    private static final TaskResponseDTO TASK_RESPONSE_DTO_2 = new TaskResponseDTO(TASK_ID_2,
            "Task 2",
            "Description 2",
            CLIENT_INN_2,
            TaskStatus.NEW,
            LocalDateTime.now(),
            "Test Name");

    private static final TaskResponseDTO TASK_RESPONSE_DTO_3 = new TaskResponseDTO(TASK_ID_3,
            "Task 3",
            "Description 3",
            CLIENT_INN_1,
            TaskStatus.NEW,
            LocalDateTime.now(),
            "Test Name");

    private static final TaskUpdateDTO TASK_UPDATE_DTO_1 = new TaskUpdateDTO("New title", "New description");

    private static final Client CLIENT_1 = new Client(CLIENT_INN_1,
            "Test Company",
            "88005553535",
            "test@test.ru",
            "Test street",
            ClientType.LEGAL_ENTITY,
            List.of(),
            USER_1);

    private static final Task TASK_1 = new Task(TASK_ID_1,  TaskStatus.NEW,  USER_1, USER_1);
    private static final Task TASK_2 = new Task(TASK_ID_2,  TaskStatus.NEW,  USER_2);
    private static final Task TASK_3 = new Task(TASK_ID_3,  TaskStatus.NEW,  USER_1);
    private static final TaskCreateDTO TASK_CREATE_DTO_1 = new TaskCreateDTO("Task 1", "Description 1");
    private static final TaskStatusDTO TASK_STATUS_DTO = new TaskStatusDTO(TaskStatus.ARCHIVE);
    private static final TaskAssigneeDTO TASK_ASSIGNEE_DTO = new TaskAssigneeDTO(USER_ID_2);





    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private TaskMapperImp taskMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Test
    void testGetTaskById_TaskFound(){
        // Arrange
        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(TASK_1));

        // Act
        Optional<Task> result = taskService.getTaskById(TASK_ID_1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TASK_1, result.get());
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
    void testGetAllTasksByClientInn(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_1)).thenReturn(Optional.of(CLIENT_1));
        when(taskRepository.findByClientInn(CLIENT_INN_1)).thenReturn(List.of(TASK_1));
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);

        // Act
        List<TaskResponseDTO> result = taskService.getAllTasksByClientInn(CLIENT_INN_1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TASK_RESPONSE_DTO_1, result.get(0));
        verify(taskRepository).findByClientInn(CLIENT_INN_1);
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
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(ACCOUNT_1));
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(USER_1));
        when(clientRepository.findByInn(CLIENT_INN_1)).thenReturn(Optional.of(CLIENT_1));
        when(taskMapper.toTask(TASK_CREATE_DTO_1, CLIENT_1, USER_1)).thenReturn(TASK_1);

        // Act
        Long savedId = taskService.saveTask(EMAIL_1, TASK_CREATE_DTO_1, CLIENT_INN_1);

        // Assert
        assertNotNull(savedId);
        assertEquals(1L, savedId);
        verify(taskRepository).save(TASK_1);
        verify(taskMapper).toTask(TASK_CREATE_DTO_1, CLIENT_1, USER_1);
        verify(clientRepository).findByInn(CLIENT_INN_1);
    }

    @Test
    void testSaveTask_ClientNotFound_ThrowsException(){
        // Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(ACCOUNT_1));
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(USER_1));
        when(clientRepository.findByInn(CLIENT_INN_1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> taskService.saveTask(EMAIL_1, TASK_CREATE_DTO_1, CLIENT_INN_1));
        verify(clientRepository).findByInn(CLIENT_INN_1);
        verify(accountRepository).findByEmail(EMAIL_1);
        verify(userRepository).findById(USER_ID_1);
    }

    @Test
    void testGetAllTasksByClientInn_ClientExists(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_1)).thenReturn(Optional.of(CLIENT_1));
        when(taskRepository.findByClientInn(CLIENT_INN_1)).thenReturn(List.of(TASK_1, TASK_3));
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);
        when(taskMapper.toTaskResponseDTO(TASK_3)).thenReturn(TASK_RESPONSE_DTO_3);

        // Act
        List<TaskResponseDTO> result = taskService.getAllTasksByClientInn(CLIENT_INN_1);

        // Assert
        assertNotNull(result);
        assertEquals(result, List.of(TASK_RESPONSE_DTO_1, TASK_RESPONSE_DTO_3));
        verify(clientRepository).findByInn(CLIENT_INN_1);
        verify(taskRepository).findByClientInn(CLIENT_INN_1);
        verify(taskMapper).toTaskResponseDTO(TASK_1);
        verify(taskMapper).toTaskResponseDTO(TASK_3);
    }

    @Test
    void testGetAllTasksByClientInn_ClientNotFound_Exception(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> taskService.getAllTasksByClientInn(CLIENT_INN_1));
        verify(clientRepository).findByInn(CLIENT_INN_1);
    }

    @Test
    void testUpdateTask_TaskExists(){
        // Arrange
        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(TASK_1));
        when(taskMapper.toTask(TASK_UPDATE_DTO_1, TASK_1)).thenReturn(TASK_1);
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);

        // Act
        TaskResponseDTO result = taskService.updateTask(TASK_ID_1, TASK_UPDATE_DTO_1);

        // Assert
        assertNotNull(result);
        assertEquals(result, TASK_RESPONSE_DTO_1);
        verify(taskRepository).findById(TASK_ID_1);
        verify(taskMapper).toTask(TASK_UPDATE_DTO_1, TASK_1);
        verify(taskMapper).toTaskResponseDTO(TASK_1);
    }

    @Test
    void testUpdateTask_TaskNotFound_Exception(){
        // Arrange
        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, ()-> taskService.updateTask(TASK_ID_1, TASK_UPDATE_DTO_1));
        verify(taskRepository).findById(TASK_ID_1);
    }

    @Test
    void getMyTasksTest(){
        // Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(ACCOUNT_1));
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(USER_1));
        when(taskRepository.findAllByUserId(USER_ID_1)).thenReturn(List.of(TASK_1, TASK_3));
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);
        when(taskMapper.toTaskResponseDTO(TASK_3)).thenReturn(TASK_RESPONSE_DTO_3);


        //Act

        List<TaskResponseDTO> result = taskService.getMyTasks(EMAIL_1);

        //Assert
        assertEquals(2, result.size());
        assertEquals(TASK_RESPONSE_DTO_1, result.get(0));
        assertEquals(TASK_RESPONSE_DTO_3, result.get(1));
        assertNotNull(result);

        verify(accountRepository).findByEmail(EMAIL_1);
        verify(userRepository).findById(USER_ID_1);
        verify(taskRepository).findAllByUserId(USER_ID_1);
        verify(taskMapper).toTaskResponseDTO(TASK_1);
        verify(taskMapper).toTaskResponseDTO(TASK_3);
    }

    @Test
    void getMyTasksTest_UserNotFound(){
        // Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () -> taskService.getMyTasks(EMAIL_1));
    }

    @Test
    void getTasksCreatedByMeTest(){
        //Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(ACCOUNT_1));
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(USER_1));

        when(taskRepository.findByAuthorUserId(USER_ID_1)).thenReturn(List.of(TASK_1, TASK_3));
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);
        when(taskMapper.toTaskResponseDTO(TASK_3)).thenReturn(TASK_RESPONSE_DTO_3);

        //Act
        List<TaskResponseDTO> result = taskService.getTasksCreatedByMe(EMAIL_1);

        //Assert
        assertNotNull(result);
        assertEquals(result, List.of(TASK_RESPONSE_DTO_1, TASK_RESPONSE_DTO_3));
        verify(taskRepository).findByAuthorUserId(USER_ID_1);
        verify(taskMapper).toTaskResponseDTO(TASK_1);
        verify(taskMapper).toTaskResponseDTO(TASK_3);
    }

    @Test
    void getTasksCreatedByMeTest_Exception(){
        //Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () -> taskService.getTasksCreatedByMe(EMAIL_1));
        verify(accountRepository).findByEmail(EMAIL_1);
    }

    @Test
    void changeStatusTest_Author() throws AccessDeniedException {
        //Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(ACCOUNT_1));
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(USER_1));

        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(TASK_1));
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);

        //Act
        TaskResponseDTO result = taskService.changeStatus(EMAIL_1, TASK_ID_1, TASK_STATUS_DTO);

        //Assert
        assertNotNull(result);
        assertEquals(result, TASK_RESPONSE_DTO_1);
        verify(accountRepository, times(2)).findByEmail(EMAIL_1);
        verify(userRepository).findById(USER_ID_1);
        verify(taskRepository).findById(TASK_ID_1);
        verify(taskMapper).toTaskResponseDTO(TASK_1);
    }

    @Test
    void changeStatusTest_Admin() throws AccessDeniedException{
        //Arrange
        when(accountRepository.findByEmail(EMAIL_2)).thenReturn(Optional.of(ACCOUNT_2));
        when(userRepository.findById(USER_ID_2)).thenReturn(Optional.of(USER_2));

        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(TASK_1));
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);

        //Act
        TaskResponseDTO result = taskService.changeStatus(EMAIL_2, TASK_ID_1, TASK_STATUS_DTO);

        //Assert
        assertNotNull(result);
        assertEquals(result, TASK_RESPONSE_DTO_1);
        verify(accountRepository, times(2)).findByEmail(EMAIL_2);
        verify(userRepository).findById(USER_ID_2);
        verify(taskRepository).findById(TASK_ID_1);
        verify(taskMapper).toTaskResponseDTO(TASK_1);
    }

    @Test
    void changeStatusTest_ExceptionUserNotFound(){
        //Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () ->
                taskService.changeStatus(EMAIL_1, TASK_ID_1, TASK_STATUS_DTO));
        verify(accountRepository).findByEmail(EMAIL_1);
    }

    @Test
    void changeStatusTest_ExceptionTaskNotFound(){
        //Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(ACCOUNT_1));
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(USER_1));
        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () ->
                taskService.changeStatus(EMAIL_1, TASK_ID_1, TASK_STATUS_DTO));
        verify(accountRepository, times(2)).findByEmail(EMAIL_1);
        verify(userRepository).findById(USER_ID_1);
        verify(taskRepository).findById(TASK_ID_1);
    }

    @Test
    void changeAssignee() throws AccessDeniedException{
        // Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.of(ACCOUNT_1));
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(USER_1));

        // Исправлено: возвращаем TASK_2 для TASK_ID_2
        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(TASK_1));
        when(userRepository.findById(USER_ID_2)).thenReturn(Optional.of(USER_2));

        // Исправлено: мокаем возврат DTO для TASK_2
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);

        // Act
        TaskResponseDTO result = taskService.changeAssignee(EMAIL_1, TASK_ID_1, TASK_ASSIGNEE_DTO);

        // Assert
        assertNotNull(result);
        assertEquals(TASK_RESPONSE_DTO_1, result);

        verify(accountRepository, times(2)).findByEmail(EMAIL_1);
        verify(userRepository).findById(USER_ID_1);
        verify(taskRepository).findById(TASK_ID_1);
        verify(userRepository).findById(USER_ID_2);
        verify(taskMapper).toTaskResponseDTO(TASK_1);
    }

    @Test
    void changeAssignee_Admin() throws AccessDeniedException{
        //Arrange
        when(accountRepository.findByEmail(EMAIL_2)).thenReturn(Optional.of(ACCOUNT_2));
        when(userRepository.findById(USER_ID_2)).thenReturn(Optional.of(USER_2));

        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(TASK_1));
        when(userRepository.findById(USER_ID_2)).thenReturn(Optional.of(USER_2));
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);

        //Act
        TaskResponseDTO result = taskService.changeAssignee(EMAIL_2, TASK_ID_1, TASK_ASSIGNEE_DTO);

        //Assert
        assertNotNull(result);
        assertEquals(result, TASK_RESPONSE_DTO_1);
        verify(accountRepository, times(2)).findByEmail(EMAIL_2);
        verify(taskRepository).findById(TASK_ID_1);
        verify(userRepository, times(2)).findById(USER_ID_2);
        verify(taskMapper).toTaskResponseDTO(TASK_1);
    }

    @Test
    void changeAssignee_ExceptionUserNotFound(){
        //Arrange
        when(accountRepository.findByEmail(EMAIL_1)).thenReturn(Optional.empty());
        //Assert
        assertThrows(NotFoundException.class, () ->
                taskService.changeAssignee(EMAIL_1, TASK_ID_2, TASK_ASSIGNEE_DTO));
        verify(accountRepository).findByEmail(EMAIL_1);
    }

    @Test
    void changeAssignee_ExceptionTaskNotFound(){
        //Arrange
        when(accountRepository.findByEmail(EMAIL_2)).thenReturn(Optional.of(ACCOUNT_2));
        when(userRepository.findById(USER_ID_2)).thenReturn(Optional.of(USER_2));
        when(taskRepository.findById(TASK_ID_2)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () ->
                taskService.changeAssignee(EMAIL_2, TASK_ID_2, TASK_ASSIGNEE_DTO));
        verify(accountRepository, times(2)).findByEmail(EMAIL_2);
        verify(userRepository).findById(USER_ID_2);
    }


}
