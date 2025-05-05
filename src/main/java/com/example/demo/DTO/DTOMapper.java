package com.example.demo.DTO;

import com.example.demo.Enum.TaskStatus;
import com.example.demo.Model.Client;
import com.example.demo.Model.Task;

import java.time.LocalDateTime;

public class DTOMapper {


    public static ClientDTO toClientDTO(Client client){
        return new ClientDTO(
                client.getInn().toString(),
                client.getName().replaceAll("\"",""),
                client.getClientType()
        );
    }

    public static Client toClient(ClientDTO clientDTO){
        Client client = new Client();
        client.setInn(Long.valueOf(clientDTO.getInn()));
        client.setName(clientDTO.getName());
        client.setClientType(clientDTO.getClientType());
        return client;
    }


    public static TaskResponseDTO toTaskResponseDTO(Task task){
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getClient().getInn(),
                task.getTaskStatus(),
                task.getCreatedAt()
        );
    }

    public static Task toTask(TaskCreateDTO taskCreateDTO, Client client){
        Task task = new Task();
        task.setTitle(taskCreateDTO.getTitle());
        task.setDescription(taskCreateDTO.getDescription());
        task.setClient(client);
        task.setTaskStatus(TaskStatus.NEW);
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }

}
