package com.example.demo.Controller;

import com.example.demo.DTO.*;
import com.example.demo.Service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService){
        this.clientService = clientService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<List<ClientInfoResponseDTO>> getAllClients(){
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/{inn}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<ClientResponseDTO> getClientByInn(@PathVariable Long inn){
        return ResponseEntity.ok(clientService.getClientByInn(inn));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<Long> createClient(@RequestBody CreateClientDTO createClientDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.saveClient(createClientDTO));
    }

    @DeleteMapping("/{inn}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteClient(@PathVariable Long inn){
        clientService.deleteClientByInn(inn);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cat/{inn}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<ClientWithTasksDTO> getClientsWithHisTasks(@PathVariable Long inn) {
        return ResponseEntity.ok(clientService.getClientsAndHisTasks(inn));
    }

    @PutMapping("/{inn}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    public ResponseEntity<ClientResponseDTO> updateClient(@PathVariable Long inn,
                                                          @RequestBody ClientForUpdateDTO clientForUpdateDTO)
    {
        ClientResponseDTO clientResponseDTO = clientService.updateClient(inn, clientForUpdateDTO);
        return ResponseEntity.ok(clientResponseDTO);
    }


}
