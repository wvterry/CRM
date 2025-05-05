package com.example.demo.Controller;

import com.example.demo.DTO.ClientDTO;
import com.example.demo.DTO.ClientWithTasksDTO;
import com.example.demo.DTO.DTOMapper;
import com.example.demo.Model.Client;
import com.example.demo.Model.Task;
import com.example.demo.Service.ClientService;
import com.example.demo.Service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    private final TaskService taskService;

    @Autowired
    public ClientController(ClientService clientService, TaskService taskService){
        this.clientService = clientService;
        this.taskService = taskService;
    }

    @GetMapping
    public List<ClientDTO> getAllClients(){
       return clientService.getAllClients()
               .stream()
               .map(DTOMapper::toClientDTO)
               .toList();
    }

    @GetMapping("/{inn}")
    public ResponseEntity<ClientDTO> getClientByInn(@PathVariable Long inn){
        return clientService.getClientByInn(inn)
                .map(DTOMapper::toClientDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ClientDTO> createClient(@RequestBody ClientDTO clientDTO){
        Client savedClient = clientService.saveClient(DTOMapper.toClient(clientDTO));
        return ResponseEntity.ok(DTOMapper.toClientDTO(savedClient));
    }

    @DeleteMapping("/{inn}")
    public void deleteClient(@PathVariable Long inn){                                        //ПРОЧИТАТЬ ПРО @PathVariable
        clientService.deleteClientByInn(inn);
    }

    @DeleteMapping("/deleteCaT/{inn}")
    public void deleteClientAndHisTasks(@PathVariable Long inn){
        clientService.deleteClientAndHisTasks(inn);
    }

   @GetMapping("/cat")
    public List<ClientWithTasksDTO> getAllClientsWithTheirTasks() {
        return clientService.getAllClientsAndTheirTasks();
    }


}
