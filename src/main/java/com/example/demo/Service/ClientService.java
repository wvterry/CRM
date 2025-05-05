package com.example.demo.Service;

import com.example.demo.DTO.ClientWithTasksDTO;
import com.example.demo.Model.Client;
import com.example.demo.Model.Task;
import com.example.demo.Repository.ClientRepository;
import com.example.demo.Repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository, TaskRepository taskRepository){
        this.clientRepository = clientRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public List<Client> getAllClients(){
        return clientRepository.findAll();
    }

    @Transactional
    public Optional<Client> getClientByInn(Long inn){
        return clientRepository.findByInn(inn);
    }

    @Transactional
    public Client saveClient(Client client){
        return clientRepository.save(client);
    }

    @Transactional
    public void deleteClientByInn(Long inn){
        clientRepository.deleteByInn(inn);
    }

    @Transactional
    public void deleteClientAndHisTasks(Long inn){
        Client client = clientRepository.findByInn(inn).orElseThrow(() -> new RuntimeException("Клиент с таким ИНН не найден"));
        taskRepository.deleteByClientInn(inn);
        clientRepository.deleteByInn(inn);
    }

    @Transactional
    public List<ClientWithTasksDTO> getAllClientsAndTheirTasks(){
        return clientRepository.findAllClientsWithTasks().stream().map(this::mapToClientWithTasksDTO).toList();
    }

    private ClientWithTasksDTO mapToClientWithTasksDTO(Client client) {
        ClientWithTasksDTO clientWithTasksDTO = new ClientWithTasksDTO();
        clientWithTasksDTO.setInn(client.getInn());
        clientWithTasksDTO.setName(client.getName());
        clientWithTasksDTO.setClientType(client.getClientType());

        if (client.getTasks() != null && !client.getTasks().isEmpty()){
            List<ClientWithTasksDTO.TaskDTO> taskDTOS = client
                    .getTasks()
                    .stream()
                    .map(task -> {
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
}
