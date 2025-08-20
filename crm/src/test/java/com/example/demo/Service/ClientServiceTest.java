package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.DTO.*;
import com.example.demo.Enum.ClientType;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Mapper.ClientMapper;
import com.example.demo.Model.Account;
import com.example.demo.Model.Client;

import com.example.demo.Model.Task;
import com.example.demo.Model.User;
import com.example.demo.Repository.AccountRepository;
import com.example.demo.Repository.ClientRepository;
import com.example.demo.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.List;
import java.util.Optional;


public class ClientServiceTest {

    private static final Long CLIENT_INN_1 = 1L;
    private static final Long CLIENT_INN_2 = 2L;

    private static final User USER_1 = new User(1L, "Egor", "Zhukov");

    private static final User USER_2 = new User(2L,"Ivan", "Ivanov");

    private static final String EMAIL = "email@email.ru";
    private static final Account ACCOUNT_1 = new Account(1L, EMAIL, USER_1);


    private static final Client CLIENT_1 = new Client(
            CLIENT_INN_1,
            "Test1",
            "88005553535",
            "test@test.ru",
            "Test street",
            ClientType.LEGAL_ENTITY,
            List.of(),
            USER_1
    );

    private static final Client CLIENT_2 = new Client(
            CLIENT_INN_2,
            "Test2",
            "88007008000",
            "test1@test.ru",
            "Test1 street",
            ClientType.LEGAL_ENTITY,
            List.of(),
            USER_2
    );

    private static final ClientResponseDTO CLIENT_RESPONSE_DTO_1 = new ClientResponseDTO(
            "Test1", "111", "test1@test.ru", "Test street1", ClientType.LEGAL_ENTITY
    );

    private static final ClientResponseDTO CLIENT_RESPONSE_DTO_2 = new ClientResponseDTO(
            "Test2", "222", "test2@test.ru", "Test street2", ClientType.LEGAL_ENTITY
    );

    private static final CreateClientDTO CREATE_CLIENT_DTO_1 = new CreateClientDTO(
            CLIENT_INN_1, "Test1", ClientType.LEGAL_ENTITY
    );

    private static final ClientForUpdateDTO CLIENT_FOR_UPDATE_DTO_1 = new ClientForUpdateDTO(
            "Test", "88005553535", "test@test.ru", "Test street"
    );

    private static final ClientInfoResponseDTO CLIENT_INFO_RESPONSE_DTO_1 = new ClientInfoResponseDTO(
            CLIENT_INN_1, "Test1", ClientType.LEGAL_ENTITY
    );

    private static final ClientInfoResponseDTO CLIENT_INFO_RESPONSE_DTO_2 = new ClientInfoResponseDTO(
            CLIENT_INN_2, "Test2", ClientType.LEGAL_ENTITY
    );

    @InjectMocks
    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllClients(){

        // Arrange
        when(clientRepository.findAll()).thenReturn(List.of(CLIENT_1, CLIENT_2));
        when(clientMapper.toClientInfoResponseDTO(CLIENT_1)).thenReturn(CLIENT_INFO_RESPONSE_DTO_1);
        when(clientMapper.toClientInfoResponseDTO(CLIENT_2)).thenReturn(CLIENT_INFO_RESPONSE_DTO_2);

        // Act
        List<ClientInfoResponseDTO> result = clientService.getAllClients();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(CLIENT_INFO_RESPONSE_DTO_1, result.get(0));
        assertEquals(CLIENT_INFO_RESPONSE_DTO_2, result.get(1));
        verify(clientRepository).findAll();
        verify(clientMapper).toClientInfoResponseDTO(CLIENT_1);
        verify(clientMapper).toClientInfoResponseDTO(CLIENT_2);
    }

    @Test
    void testGetClientByInn_ClientExist(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_1)).thenReturn(Optional.of(CLIENT_1));
        when(clientMapper.toClientResponseDTO(CLIENT_1)).thenReturn(CLIENT_RESPONSE_DTO_1);

        // Act
        ClientResponseDTO result = clientService.getClientByInn(CLIENT_INN_1);

        // Assert
        assertNotNull(result);
        assertEquals(result, CLIENT_RESPONSE_DTO_1);
        verify(clientRepository).findByInn(CLIENT_INN_1);
        verify(clientMapper).toClientResponseDTO(CLIENT_1);
    }

    @Test
    void testGetClientByInn_ClientNotFound_Exception(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> clientService.getClientByInn(CLIENT_INN_1));
        verify(clientRepository).findByInn(CLIENT_INN_1);
    }

    @Test
    void testSaveClient(){
        // Arrange
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(ACCOUNT_1));
        when(userRepository.findById(ACCOUNT_1.getUser().getUserId())).thenReturn(Optional.of(USER_1));
        when(clientMapper.toClient(CREATE_CLIENT_DTO_1)).thenReturn(CLIENT_1);

        // Act
        Long result = clientService.saveClient(EMAIL, CREATE_CLIENT_DTO_1);

        // Assert
        assertNotNull(result);
        assertEquals(CLIENT_INN_1, result);
        verify(clientMapper).toClient(CREATE_CLIENT_DTO_1);
        verify(clientRepository).save(CLIENT_1);
        verify(accountRepository).findByEmail(EMAIL);
        verify(userRepository).findById(ACCOUNT_1.getUser().getUserId());
    }

    @Test
    void testDeleteClientByInn_ClientExist(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_1)).thenReturn(Optional.of(CLIENT_1));

        // Act
        clientService.deleteClientByInn(CLIENT_INN_1);

        // Assert
        verify(clientRepository).findByInn(CLIENT_INN_1);
        verify(clientRepository).deleteByInn(CLIENT_INN_1);
    }

    @Test
    void testDeleteClientByInn_ClientNotExist_Exception(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> clientService.deleteClientByInn(CLIENT_INN_1));
        verify(clientRepository).findByInn(CLIENT_INN_1);

    }

    @Test
    void testGetClientsAndHisTasks_ClientExist(){

        Task task1 = new Task();
        Task task2 = new Task();
        List<Task> tasks = List.of(task1, task2);
        CLIENT_1.setTasks(tasks);
        ClientWithTasksDTO.TaskDTO taskDTO1 = new ClientWithTasksDTO.TaskDTO();
        ClientWithTasksDTO.TaskDTO taskDTO2 = new ClientWithTasksDTO.TaskDTO();
        List<ClientWithTasksDTO.TaskDTO> taskDTOS = List.of(taskDTO1, taskDTO2);
        ClientWithTasksDTO clientWithTasksDTO = new ClientWithTasksDTO(CLIENT_INN_1, "Test", ClientType.LEGAL_ENTITY, taskDTOS);

        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_1)).thenReturn(Optional.of(CLIENT_1));
        when(clientMapper.toClientAndHisTasksDTO(CLIENT_1)).thenReturn(clientWithTasksDTO);

        // Act
        ClientWithTasksDTO result = clientService.getClientsAndHisTasks(CLIENT_INN_1);

        // Assert
        assertNotNull(result);
        assertEquals(result, clientWithTasksDTO);
        verify(clientRepository).findByInn(CLIENT_INN_1);
        verify(clientMapper).toClientAndHisTasksDTO(CLIENT_1);
    }

    @Test
    void testGetClientsAndHisTasks_ClientNotFound_Exception(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> clientService.getClientsAndHisTasks(CLIENT_INN_1));
        verify(clientRepository).findByInn(CLIENT_INN_1);
    }

//    @Test
//    void testUpdateClient_ClientExist(){
//        // Arrange
//        when(clientRepository.findByInn(CLIENT_INN_1)).thenReturn(Optional.of(CLIENT_1));
//        when(clientMapper.toClient(CLIENT_1, CLIENT_FOR_UPDATE_DTO_1)).thenReturn(CLIENT_1);
//        when(clientMapper.toClientResponseDTO(CLIENT_1)).thenReturn(CLIENT_RESPONSE_DTO_1);
//
//        // Act
//        ClientResponseDTO result = clientService.updateClient(CLIENT_INN_1, CLIENT_FOR_UPDATE_DTO_1);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(result, CLIENT_RESPONSE_DTO_1);
//        verify(clientRepository).findByInn(CLIENT_INN_1);
//        verify(clientMapper).toClient(CLIENT_1, CLIENT_FOR_UPDATE_DTO_1);
//        verify(clientMapper).toClientResponseDTO(CLIENT_1);
//        verify(clientRepository).save(CLIENT_1);
//    }

    @Test
    void testUpdateClient_ClientNotFound_Exception(){
        // Arrange
        when(clientRepository.findByInn(CLIENT_INN_1)).thenReturn(Optional.empty());

        // Assert
        assertThrows(NotFoundException.class, () -> clientService.updateClient(CLIENT_INN_1, CLIENT_FOR_UPDATE_DTO_1));
        verify(clientRepository).findByInn(CLIENT_INN_1);
    }
}
