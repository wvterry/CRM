package com.example.demo.Mapper;

import com.example.demo.DTO.*;
import com.example.demo.Model.Client;
import com.example.demo.Model.User;
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

    public Client toClient(User user, CreateClientDTO createClientDTO) {
        Client client = new Client();
        client.setInn(createClientDTO.getInn());
        client.setName(createClientDTO.getName());
        client.setClientType(createClientDTO.getClientType());
        client.setManager(user);
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


    public Client toClient(Client clientForUpdate, ClientForUpdateDTO clientForUpdateDTO){
        clientForUpdate.setPhone(clientForUpdateDTO.getPhone());
        clientForUpdate.setEmail(clientForUpdateDTO.getEmail());
        clientForUpdate.setAddress(clientForUpdateDTO.getAddress());
        clientForUpdate.setName(clientForUpdateDTO.getName());
        return clientForUpdate;
    }



    public ClientResponseDTO toClientResponseDTO(Client client){
        ClientResponseDTO clientResponseDTO = new ClientResponseDTO();
        clientResponseDTO.setName(client.getName());
        clientResponseDTO.setAddress(client.getAddress());
        clientResponseDTO.setEmail(client.getEmail());
        clientResponseDTO.setPhone(client.getPhone());
        clientResponseDTO.setClientType(client.getClientType());
        return clientResponseDTO;
    }

}


