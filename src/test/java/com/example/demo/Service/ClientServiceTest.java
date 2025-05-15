package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.DTO.*;
import com.example.demo.Enum.ClientType;
import com.example.demo.Enum.TaskStatus;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Mapper.ClientMapper;
import com.example.demo.Model.Client;

import com.example.demo.Model.Task;
import com.example.demo.Repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ClientServiceTest {

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
        Client client1 = new Client(555L, "Test1",
                "88005553535", "test@test.ru", "Test street", ClientType.LEGAL_ENTITY, List.of());

        Client client2 = new Client(666L, "Test2",
                "88007008000", "test1@test.ru","Test1 street", ClientType.LEGAL_ENTITY, List.of());

        ClientInfoResponseDTO clientInfoResponseDTO1 = new ClientInfoResponseDTO(555L, "Test1", ClientType.LEGAL_ENTITY);

        ClientInfoResponseDTO clientInfoResponseDTO2 = new ClientInfoResponseDTO(666L, "Test2", ClientType.LEGAL_ENTITY);

        when(clientRepository.findAll()).thenReturn(List.of(client1, client2));

        when(clientMapper.toClientInfoResponseDTO(client1)).thenReturn(clientInfoResponseDTO1);
        when(clientMapper.toClientInfoResponseDTO(client2)).thenReturn(clientInfoResponseDTO2);

        List<ClientInfoResponseDTO> result = clientService.getAllClients();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(clientInfoResponseDTO1, result.get(0));
        assertEquals(clientInfoResponseDTO2, result.get(1));

        verify(clientRepository).findAll();
        verify(clientMapper).toClientInfoResponseDTO(client1);
        verify(clientMapper).toClientInfoResponseDTO(client2);
    }

    @Test
    void testGetClientByInn_ClientExist(){
        Long inn = 666L;
        Client client1 = new Client(inn, "Test1",
                "88005553535", "test@test.ru", "Test street", ClientType.LEGAL_ENTITY, List.of());

        ClientResponseDTO clientResponseDTO = new ClientResponseDTO("Test1", "8800700800",
                "test@test.ru", "Test street", ClientType.LEGAL_ENTITY);

        when(clientRepository.findByInn(inn)).thenReturn(Optional.of(client1));
        when(clientMapper.toClientResponseDTOFromClient(client1)).thenReturn(clientResponseDTO);

        ClientResponseDTO result = clientService.getClientByInn(inn);

        assertNotNull(result);
        assertEquals(result, clientResponseDTO);
        verify(clientRepository).findByInn(inn);
        verify(clientMapper).toClientResponseDTOFromClient(client1);
    }

    @Test
    void testGetClientByInn_ClientNotFound_Exception(){
        Long inn = 666L;

        when(clientRepository.findByInn(inn)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.getClientByInn(inn));
        verify(clientRepository).findByInn(inn);
    }

    @Test
    void testSaveClient(){
        Long inn = 555L;
        CreateClientDTO createClientDTO = new CreateClientDTO(555L, "Test", ClientType.LEGAL_ENTITY);
        Client client1 = new Client(555L, "Test",
                "88005553535", "test@test.ru", "Test street", ClientType.LEGAL_ENTITY, List.of());

        when(clientMapper.toClient(createClientDTO)).thenReturn(client1);

        Long result = clientService.saveClient(createClientDTO);

        assertNotNull(result);
        assertEquals(inn, result);
        verify(clientMapper).toClient(createClientDTO);
        verify(clientRepository).save(client1);
    }

    @Test
    void testDeleteClientByInn_ClientExist(){
        Long inn = 555L;
        Client client1 = new Client(555L, "Test",
                "88005553535", "test@test.ru", "Test street", ClientType.LEGAL_ENTITY, List.of());

        when(clientRepository.findByInn(inn)).thenReturn(Optional.of(client1));

        clientService.deleteClientByInn(inn);

        verify(clientRepository).findByInn(inn);
        verify(clientRepository).deleteByInn(inn);
    }

    @Test
    void testDeleteClientByInn_ClientNotExist_Exception(){
        Long inn = 555L;

        when(clientRepository.findByInn(inn)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.deleteClientByInn(inn));
        verify(clientRepository).findByInn(inn);

    }

    @Test
    void testGetClientsAndHisTasks_ClientExist(){
        Long inn = 555L;
        Client client1 = new Client();

        Task task1 = new Task();
        Task task2 = new Task();

        List<Task> tasks = List.of(task1, task2);

        client1.setInn(inn);
        client1.setName("Test");
        client1.setPhone("88005553535");
        client1.setEmail("test@test.ru");
        client1.setAddress("Test street");
        client1.setClientType(ClientType.LEGAL_ENTITY);
        client1.setTasks(tasks);

        ClientWithTasksDTO.TaskDTO taskDTO1 = new ClientWithTasksDTO.TaskDTO();
        ClientWithTasksDTO.TaskDTO taskDTO2 = new ClientWithTasksDTO.TaskDTO();

        List<ClientWithTasksDTO.TaskDTO> taskDTOS = List.of(taskDTO1, taskDTO2);

        ClientWithTasksDTO clientWithTasksDTO = new ClientWithTasksDTO(inn, "Test", ClientType.LEGAL_ENTITY, taskDTOS);

        when(clientRepository.findByInn(inn)).thenReturn(Optional.of(client1));
        when(clientMapper.toClientAndHisTasksDTO(client1)).thenReturn(clientWithTasksDTO);

        ClientWithTasksDTO result = clientService.getClientsAndHisTasks(inn);

        assertNotNull(result);
        assertEquals(result, clientWithTasksDTO);
        verify(clientRepository).findByInn(inn);
        verify(clientMapper).toClientAndHisTasksDTO(client1);
    }

    @Test
    void testGetClientsAndHisTasks_ClientNotFound_Exception(){
        Long inn = 555L;
        when(clientRepository.findByInn(inn)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.getClientsAndHisTasks(inn));
        verify(clientRepository).findByInn(inn);
    }

    @Test
    void testUpdateClient_ClientExist(){
        Long inn = 555L;
        Client client1 = new Client(555L, "Test",
                "88005553535", "test@test.ru", "Test street", ClientType.LEGAL_ENTITY, List.of());

        ClientForUpdateDTO clientForUpdateDTO = new ClientForUpdateDTO("Test", "88005553535", "test@test.ru", "Test street");
        ClientResponseDTO clientResponseDTO = new ClientResponseDTO("Test", "88005553535", "test@test.ru", "Test street", ClientType.LEGAL_ENTITY);

        when(clientRepository.findByInn(inn)).thenReturn(Optional.of(client1));
        when(clientMapper.toClientFromClientForUpdateDTO(client1, clientForUpdateDTO)).thenReturn(client1);
        when(clientMapper.toClientResponseDTOFromClient(client1)).thenReturn(clientResponseDTO);

        ClientResponseDTO result = clientService.updateClient(inn, clientForUpdateDTO);


        assertNotNull(result);
        assertEquals(result, clientResponseDTO);
        verify(clientRepository).findByInn(inn);
        verify(clientMapper).toClientFromClientForUpdateDTO(client1, clientForUpdateDTO);
        verify(clientMapper).toClientResponseDTOFromClient(client1);
        verify(clientRepository).save(client1);
    }

    @Test
    void testUpdateClient_ClientNotFound_Exception(){
        Long inn = 555L;
        ClientForUpdateDTO clientForUpdateDTO = new ClientForUpdateDTO();

        when(clientRepository.findByInn(inn)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.updateClient(inn, clientForUpdateDTO));
        verify(clientRepository).findByInn(inn);
    }
}
