package com.example.demo.Service;

import com.example.demo.DTO.*;
import com.example.demo.Enum.ClientType;
import com.example.demo.Enum.TaskStatus;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.JWT.AuthRequest;
import com.example.demo.JWT.JwtUtil;
import com.example.demo.Mapper.TaskMapperImp;
import com.example.demo.Model.Client;
import com.example.demo.Model.Role;
import com.example.demo.Model.Task;
import com.example.demo.Model.User;
import com.example.demo.Repository.ClientRepository;
import com.example.demo.Repository.TaskRepository;
import com.example.demo.Repository.UserRepository;
import io.jsonwebtoken.lang.Assert;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskServiceTest {

    private static final String email = "test@test.ru";
    private static final String password = "password";
    private static final Long CLIENT_INN_555 = 555L;
    private static final Long TASK_ID_1 = 1L;
    private static final Long TASK_ID_2 = 2L;
    private static final Long USER_ID_1 = 111L;
    private static final Long USER_ID_2 = 222L;
    private static final Long TASK_ID_111 = 111L;
    private static final Long TASK_ID_222 = 222L;
    private static final Role USER_ROLE = new Role("USER");
    private static final Role ADMIN_ROLE = new Role("ADMIN");
    private static final User AUTHOR_USER = new User(100L, email, Set.of(USER_ROLE));
    private static final User ADMIN_USER = new User(200L, "admin@example.com", Set.of(ADMIN_ROLE));
    private static final User ASSIGNEE_USER_2 = new User(USER_ID_2, email, Set.of(USER_ROLE));
    private static final User ASSIGNEE_USER_1 = new User(USER_ID_1, email, Set.of(USER_ROLE));
    Task TASK_111 = new Task(TASK_ID_111, TaskStatus.ON_HOLD, AUTHOR_USER);
    Task TASK_222 = new Task(TASK_ID_222, USER_1);
    TaskStatusDTO archiveStatusDTO = new TaskStatusDTO(TaskStatus.ARCHIVE);
    TaskAssigneeDTO taskAssigneeDTO = new TaskAssigneeDTO(USER_ID_2);
    private final static com.example.demo.Model.User USER_1 = new com.example.demo.Model.User(
            USER_ID_1,
            "Egor",
            "Zhukov",
            email,
            password
    );

    private final static com.example.demo.Model.User USER_2 = new com.example.demo.Model.User(
            USER_ID_2,
            "Ivan",
            "Ivanov",
            "ii@test.ru",
            password
    );
    private static final User user1 = new User(
            "Egor",
            "Zhukov",
            "ez@test.ru",
            "ez"
    );

    private static final User user2 = new User(
            "Ivan",
            "Ivanov",
            "ii@test.ru",
            "ii"
    );

    private static final Client CLIENT_555 = new Client(
            CLIENT_INN_555,
            "Test Company",
            "88005553535",
            "test@test.ru",
            "Test street",
            ClientType.LEGAL_ENTITY,
            List.of(),
            user1
    );

    private static final Task TASK_1 = new Task(
            TASK_ID_1,
            "Task 1",
            "Description 1",
            CLIENT_555,
            TaskStatus.NEW,
            LocalDateTime.now(),
            user1,
            user2
    );

    private static final Task TASK_2 = new Task(
            TASK_ID_2,
            "Task 2",
            "Description 2",
            CLIENT_555,
            TaskStatus.IN_PROGRESS,
            LocalDateTime.now(),
            user2,
            user1
    );


    private static final TaskResponseDTO TASK_RESPONSE_DTO_1 = new TaskResponseDTO(
            TASK_ID_1,
            "Task 1",
            "Description 1",
            CLIENT_INN_555,
            TaskStatus.NEW,
            LocalDateTime.now(),
            "Test Name"
    );

    private static final TaskResponseDTO TASK_RESPONSE_DTO_111 = new TaskResponseDTO(
            TASK_ID_111,
            "Task 1",
            "Description 1",
            CLIENT_INN_555,
            TaskStatus.NEW,
            LocalDateTime.now(),
            "Test Name"
    );

    private static final TaskResponseDTO TASK_RESPONSE_DTO_222 = new TaskResponseDTO(
            TASK_ID_222,
            "Task 1",
            "Description 1",
            CLIENT_INN_555,
            TaskStatus.NEW,
            LocalDateTime.now(),
            "Test Name"
    );

    private static final TaskResponseDTO TASK_RESPONSE_DTO_2 = new TaskResponseDTO(
            TASK_ID_2,
            "Task 2",
            "Description 2",
            CLIENT_INN_555,
            TaskStatus.IN_PROGRESS,
            LocalDateTime.now(),
            "Test Name2"
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

    @Mock
    private UserRepository userRepository;

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
        when(taskMapper.toTask(TASK_UPDATE_DTO, TASK_1)).thenReturn(TASK_1);
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);

        // Act
        TaskResponseDTO result = taskService.updateTask(TASK_ID_1, TASK_UPDATE_DTO);

        // Assert
        assertNotNull(result);
        assertEquals(result, TASK_RESPONSE_DTO_1);
        verify(taskRepository).findById(TASK_ID_1);
        verify(taskMapper).toTask(TASK_UPDATE_DTO, TASK_1);
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

    @Test
    void getMyTasksTest(){
        // Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(USER_1));
        when(taskRepository.findAllByUserId(USER_ID_1)).thenReturn(List.of(TASK_1, TASK_2));
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);
        when(taskMapper.toTaskResponseDTO(TASK_2)).thenReturn(TASK_RESPONSE_DTO_2);

        //Act
        List<TaskResponseDTO> result = taskService.getMyTasks(email);

        //Assert
        assertNotNull(result);
        assertEquals(result, List.of(TASK_RESPONSE_DTO_1, TASK_RESPONSE_DTO_2));
    }

    @Test
    void getMyTasksTest_UserNotFound(){
        // Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () -> taskService.getMyTasks(email));
    }

    @Test
    void getTasksCreatedByMeTest(){
        //Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(USER_1));

        when(taskRepository.findByAuthorUserId(USER_ID_1)).thenReturn(List.of(TASK_1, TASK_2));
        when(taskMapper.toTaskResponseDTO(TASK_1)).thenReturn(TASK_RESPONSE_DTO_1);
        when(taskMapper.toTaskResponseDTO(TASK_2)).thenReturn(TASK_RESPONSE_DTO_2);

        //Act
        List<TaskResponseDTO> result = taskService.getTasksCreatedByMe(email);

        //Assert
        assertNotNull(result);
        assertEquals(result, List.of(TASK_RESPONSE_DTO_1, TASK_RESPONSE_DTO_2));
        verify(taskRepository).findByAuthorUserId(USER_ID_1);
        verify(taskMapper).toTaskResponseDTO(TASK_1);
        verify(taskMapper).toTaskResponseDTO(TASK_2);
    }

    @Test
    void getTasksCreatedByMeTest_Exception(){
        //Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () -> taskService.getTasksCreatedByMe(email));
        verify(userRepository).findUserByEmail(email);
    }

    @Test
    void changeStatusTest_Author() throws AccessDeniedException {
        //Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(AUTHOR_USER));
        when(taskRepository.findById(TASK_ID_111)).thenReturn(Optional.of(TASK_111));
        when(taskMapper.toTaskResponseDTO(TASK_111)).thenReturn(TASK_RESPONSE_DTO_111);

        //Act
        TaskResponseDTO result = taskService.changeStatus(email, TASK_ID_111, archiveStatusDTO);

        //Assert
        assertNotNull(result);
        assertEquals(result, TASK_RESPONSE_DTO_111);
        verify(userRepository).findUserByEmail(email);
        verify(taskRepository).findById(TASK_ID_111);
        verify(taskMapper).toTaskResponseDTO(TASK_111);
    }

    @Test
    void changeStatusTest_Admin() throws AccessDeniedException{
        //Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(ADMIN_USER));
        when(taskRepository.findById(TASK_ID_111)).thenReturn(Optional.of(TASK_111));
        when(taskMapper.toTaskResponseDTO(TASK_111)).thenReturn(TASK_RESPONSE_DTO_111);

        //Act
        TaskResponseDTO result = taskService.changeStatus(email, TASK_ID_111, archiveStatusDTO);

        //Assert
        assertNotNull(result);
        assertEquals(result, TASK_RESPONSE_DTO_111);
        verify(userRepository).findUserByEmail(email);
        verify(taskRepository).findById(TASK_ID_111);
        verify(taskMapper).toTaskResponseDTO(TASK_111);
    }

    @Test
    void changeStatusTest_ExceptionUserNotFound(){
        //Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () ->
                taskService.changeStatus(email, TASK_ID_111, archiveStatusDTO));
        verify(userRepository).findUserByEmail(email);
    }

    @Test
    void changeStatusTest_ExceptionTaskNotFound(){
        //Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(USER_1));
        when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () ->
                taskService.changeStatus(email, TASK_ID_1, archiveStatusDTO));
        verify(userRepository).findUserByEmail(email);
        verify(taskRepository).findById(TASK_ID_1);
    }

    @Test
    void changeAssignee() throws AccessDeniedException{
        //Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(ASSIGNEE_USER_1));
        when(taskRepository.findById(TASK_ID_222)).thenReturn(Optional.of(TASK_222));
        when(userRepository.findById(USER_ID_2)).thenReturn(Optional.of(ASSIGNEE_USER_2));
        when(taskMapper.toTaskResponseDTO(TASK_222)).thenReturn(TASK_RESPONSE_DTO_222);

        //Act
        TaskResponseDTO result = taskService.changeAssignee(email, TASK_ID_222, taskAssigneeDTO);

        //Assert
        assertNotNull(result);
        assertEquals(result, TASK_RESPONSE_DTO_222);
        verify(userRepository).findUserByEmail(email);
        verify(taskRepository).findById(TASK_ID_222);
        verify(userRepository).findById(USER_ID_2);
        verify(taskMapper).toTaskResponseDTO(TASK_222);
    }

    @Test
    void changeAssignee_Admin() throws AccessDeniedException{
        //Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(ADMIN_USER));
        when(taskRepository.findById(TASK_ID_222)).thenReturn(Optional.of(TASK_222));
        when(userRepository.findById(USER_ID_2)).thenReturn(Optional.of(ASSIGNEE_USER_2));
        when(taskMapper.toTaskResponseDTO(TASK_222)).thenReturn(TASK_RESPONSE_DTO_222);

        //Act
        TaskResponseDTO result = taskService.changeAssignee(email, TASK_ID_222, taskAssigneeDTO);

        //Assert
        assertNotNull(result);
        assertEquals(result, TASK_RESPONSE_DTO_222);
        verify(userRepository).findUserByEmail(email);
        verify(taskRepository).findById(TASK_ID_222);
        verify(userRepository).findById(USER_ID_2);
        verify(taskMapper).toTaskResponseDTO(TASK_222);
    }

    @Test
    void changeAssignee_ExceptionUserNotFound(){
        //Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () ->
                taskService.changeAssignee(email, TASK_ID_222, taskAssigneeDTO));
        verify(userRepository).findUserByEmail(email);
    }

    @Test
    void changeAssignee_ExceptionTaskNotFound(){
        //Arrange
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(ADMIN_USER));
        when(taskRepository.findById(TASK_ID_222)).thenReturn(Optional.empty());

        //Assert
        assertThrows(NotFoundException.class, () ->
                taskService.changeAssignee(email, TASK_ID_222, taskAssigneeDTO));
        verify(userRepository).findUserByEmail(email);
    }


}
