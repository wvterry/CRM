package com.example.demo.Mapper;

import com.example.demo.DTO.*;
import com.example.demo.Model.Client;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientMapperImp implements ClientMapper {

    public ClientInfoResponseDTO toClientInfoResponseDTO(Client client) {
        return new ClientInfoResponseDTO(
                client.getInn(),
                client.getName().replaceAll("\"", ""),
                client.getClientType()
        );
    }

    public Client toClient(CreateClientDTO createClientDTO) {
        Client client = new Client();
        client.setInn(createClientDTO.getInn());
        client.setName(createClientDTO.getName());
        client.setClientType(createClientDTO.getClientType());
        return client;
    }

    public ClientWithTasksDTO toClientAndHisTasksDTO(Client client) {
        ClientWithTasksDTO clientWithTasksDTO = new ClientWithTasksDTO();
        clientWithTasksDTO.setInn(client.getInn());
        clientWithTasksDTO.setName(client.getName());
        clientWithTasksDTO.setClientType(client.getClientType());

        if (!client.getTasks().isEmpty()) {
            List<ClientWithTasksDTO.TaskDTO> taskDTOS = client.getTasks().stream().map(task -> {
                ClientWithTasksDTO.TaskDTO taskDTO = new ClientWithTasksDTO.TaskDTO();
                taskDTO.setId(task.getId());
                taskDTO.setTitle(task.getTitle());
                taskDTO.setDescription(task.getDescription());
                taskDTO.setTaskStatus(task.getTaskStatus());
                taskDTO.setCreatedAt(task.getCreatedAt());
                return taskDTO;
            }).toList();
        clientWithTasksDTO.setTasks(taskDTOS);
        }
        return clientWithTasksDTO;
    }

    public Client toClientFromClientForUpdateDTO(Client clientForUpdate, ClientForUpdateDTO clientForUpdateDTO){
        Client client = new Client();
        client.setInn(clientForUpdate.getInn());
        client.setPhone(clientForUpdateDTO.getPhone());
        client.setEmail(clientForUpdateDTO.getEmail());
        client.setAddress(clientForUpdateDTO.getAddress());
        client.setName(clientForUpdateDTO.getName());
        client.setClientType(clientForUpdate.getClientType());
        client.setTasks(clientForUpdate.getTasks());
        return client;
    }

    public ClientResponseDTO toClientResponseDTOFromClient(Client client){
        ClientResponseDTO clientResponseDTO = new ClientResponseDTO();
        clientResponseDTO.setName(client.getName());
        clientResponseDTO.setAddress(client.getAddress());
        clientResponseDTO.setEmail(client.getEmail());
        clientResponseDTO.setPhone(client.getPhone());
        clientResponseDTO.setClientType(client.getClientType());
        return clientResponseDTO;
    }

}


