package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.DTO.*;
import com.example.demo.Enum.ClientType;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Mapper.ClientMapper;
import com.example.demo.Model.Client;

import com.example.demo.Model.Task;
import com.example.demo.Model.User;
import com.example.demo.Repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.List;
import java.util.Optional;

public class ClientServiceTest {

    private static final Long CLIENT_INN_555 = 555L;
    private static final Long CLIENT_INN_666 = 666L;

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
            "Test1",
            "88005553535",
            "test@test.ru",
            "Test street",
            ClientType.LEGAL_ENTITY,
            List.of(),
            user1
    );

    private static final Client CLIENT_666 = new Client(
            CLIENT_INN_666,
            "Test2",
            "88007008000",
            "test1@test.ru",
            "Test1 street",
            ClientType.LEGAL_ENTITY,
            List.of(),
            user2
    );

    private static final ClientResponseDTO CLIENT_RESPONSE_DTO_555 = new ClientResponseDTO(
            "Test1", "88005553535", "test@test.ru", "Test street", ClientType.LEGAL_ENTITY
    );

    private static final CreateClientDTO CREATE_CLIENT_DTO_555 = new CreateClientDTO(
            CLIENT_INN_555, "Test1", ClientType.LEGAL_ENTITY
    );

    private static final ClientForUpdateDTO CLIENT_FOR_UPDATE_DTO = new ClientForUpdateDTO(
            "Test", "88005553535", "test@test.ru", "Test street"
    );

    private static final ClientInfoResponseDTO CLIENT_INFO_RESPONSE_DTO_555 = new ClientInfoResponseDTO(
            555L, "Test1", ClientType.LEGAL_ENTITY
    );

    private static final ClientInfoResponseDTO CLIENT_INFO_RESPONSE_DTO_666 = new ClientInfoResponseDTO(
            666L, "Test2", ClientType.LEGAL_ENTITY
    );

    @InjectMocks
    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllClients(){

        // Arrange
        when(clientRepository.findAll()).thenReturn(List.of(CLIENT_555, CLIENT_666));
        when(clientMapper.toClientInfoResponseDTO(CLIENT_555)).thenReturn(CLIENT_INFO_RESPONSE_DTO_555);
        when(clientMapper.toClientInfoResponseDTO(CLIENT_666)).thenReturn(CLIENT_INFO_RESPONSE_DTO_666);

        // Act
        List<ClientInfoResponseDTO> result = clientService.getAllClients();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(CLIENT_INFO_RESPONSE_DTO_555, result.get(0));
        assertEquals(CLIENT_INFO_RESPONSE_DTO_666, result.get(1));
        verify(clientRepository).findAll();
        verify(clientMapper).toClientInfoResponseDTO(CLIENT_555);
        verify(clientMapper).toClientInfoResponseDTO(CLIENT_666);
    }

    @Test
    void testGetClientByInn_ClientExist(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_555)).thenReturn(Optional.of(CLIENT_555));
        when(clientMapper.toClientResponseDTO(CLIENT_555)).thenReturn(CLIENT_RESPONSE_DTO_555);

        // Act
        ClientResponseDTO result = clientService.getClientByInn(CLIENT_INN_555);

        // Assert
        assertNotNull(result);
        assertEquals(result, CLIENT_RESPONSE_DTO_555);
        verify(clientRepository).findByInn(CLIENT_INN_555);
        verify(clientMapper).toClientResponseDTO(CLIENT_555);
    }

    @Test
    void testGetClientByInn_ClientNotFound_Exception(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_555)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> clientService.getClientByInn(CLIENT_INN_555));
        verify(clientRepository).findByInn(CLIENT_INN_555);
    }

    @Test
    void testSaveClient(){
        // Arrange
        when(clientMapper.toClient(CREATE_CLIENT_DTO_555)).thenReturn(CLIENT_555);

        // Act
        Long result = clientService.saveClient(CREATE_CLIENT_DTO_555);

        // Assert
        assertNotNull(result);
        assertEquals(CLIENT_INN_555, result);
        verify(clientMapper).toClient(CREATE_CLIENT_DTO_555);
        verify(clientRepository).save(CLIENT_555);
    }

    @Test
    void testDeleteClientByInn_ClientExist(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_555)).thenReturn(Optional.of(CLIENT_555));

        // Act
        clientService.deleteClientByInn(CLIENT_INN_555);

        // Assert
        verify(clientRepository).findByInn(CLIENT_INN_555);
        verify(clientRepository).deleteByInn(CLIENT_INN_555);
    }

    @Test
    void testDeleteClientByInn_ClientNotExist_Exception(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_555)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> clientService.deleteClientByInn(CLIENT_INN_555));
        verify(clientRepository).findByInn(CLIENT_INN_555);

    }

    @Test
    void testGetClientsAndHisTasks_ClientExist(){

        Task task1 = new Task();
        Task task2 = new Task();
        List<Task> tasks = List.of(task1, task2);
        CLIENT_555.setTasks(tasks);
        ClientWithTasksDTO.TaskDTO taskDTO1 = new ClientWithTasksDTO.TaskDTO();
        ClientWithTasksDTO.TaskDTO taskDTO2 = new ClientWithTasksDTO.TaskDTO();
        List<ClientWithTasksDTO.TaskDTO> taskDTOS = List.of(taskDTO1, taskDTO2);
        ClientWithTasksDTO clientWithTasksDTO = new ClientWithTasksDTO(CLIENT_INN_555, "Test", ClientType.LEGAL_ENTITY, taskDTOS);

        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_555)).thenReturn(Optional.of(CLIENT_555));
        when(clientMapper.toClientAndHisTasksDTO(CLIENT_555)).thenReturn(clientWithTasksDTO);

        // Act
        ClientWithTasksDTO result = clientService.getClientsAndHisTasks(CLIENT_INN_555);

        // Assert
        assertNotNull(result);
        assertEquals(result, clientWithTasksDTO);
        verify(clientRepository).findByInn(CLIENT_INN_555);
        verify(clientMapper).toClientAndHisTasksDTO(CLIENT_555);
    }

    @Test
    void testGetClientsAndHisTasks_ClientNotFound_Exception(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_555)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> clientService.getClientsAndHisTasks(CLIENT_INN_555));
        verify(clientRepository).findByInn(CLIENT_INN_555);
    }

    @Test
    void testUpdateClient_ClientExist(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_555)).thenReturn(Optional.of(CLIENT_555));
        when(clientMapper.toClient(CLIENT_555, CLIENT_FOR_UPDATE_DTO)).thenReturn(CLIENT_555);
        when(clientMapper.toClientResponseDTO(CLIENT_555)).thenReturn(CLIENT_RESPONSE_DTO_555);

        // Act
        ClientResponseDTO result = clientService.updateClient(CLIENT_INN_555, CLIENT_FOR_UPDATE_DTO);

        // Assert
        assertNotNull(result);
        assertEquals(result, CLIENT_RESPONSE_DTO_555);
        verify(clientRepository).findByInn(CLIENT_INN_555);
        verify(clientMapper).toClient(CLIENT_555, CLIENT_FOR_UPDATE_DTO);
        verify(clientMapper).toClientResponseDTO(CLIENT_555);
        verify(clientRepository).save(CLIENT_555);
    }

    @Test
    void testUpdateClient_ClientNotFound_Exception(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_555)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> clientService.updateClient(CLIENT_INN_555, CLIENT_FOR_UPDATE_DTO));
        verify(clientRepository).findByInn(CLIENT_INN_555);
    }
}
