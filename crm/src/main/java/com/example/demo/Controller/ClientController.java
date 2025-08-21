package com.example.demo.Controller;

import com.example.demo.DTO.*;
import com.example.demo.JWT.JwtTokenService;
import com.example.demo.Service.ClientService;
import com.example.securitycommon.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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

    private final JwtUtil jwtUtil;

    @Autowired
    public ClientController(ClientService clientService, JwtUtil jwtUtil){
        this.clientService = clientService;
        this.jwtUtil = jwtUtil;
    }


    @GetMapping
    public ResponseEntity<List<ClientInfoResponseDTO>> getAllClients(){
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/{inn}")
    public ResponseEntity<ClientResponseDTO> getClientByInn(@PathVariable("inn") Long inn){
        return ResponseEntity.ok(clientService.getClientByInn(inn));
    }

    @PostMapping
    public ResponseEntity<Long> createClient(HttpServletRequest httpServletRequest, @RequestBody CreateClientDTO createClientDTO){
        String token = jwtUtil.getTokenFromRequest(httpServletRequest);
        String creatorEmail = jwtUtil.getEmailFromToken(token);
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.saveClient(creatorEmail, createClientDTO));
    }

    @DeleteMapping("/{inn}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClient(@PathVariable("inn") Long inn){
        clientService.deleteClientByInn(inn);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cat/{inn}")
    public ResponseEntity<ClientWithTasksDTO> getClientsWithHisTasks(@PathVariable("inn") Long inn) {
        return ResponseEntity.ok(clientService.getClientsAndHisTasks(inn));
    }

    @PutMapping("/{inn}")
    public ResponseEntity<ClientResponseDTO> updateClient(@PathVariable("inn") Long inn,
                                                          @RequestBody ClientForUpdateDTO clientForUpdateDTO)
    {
        ClientResponseDTO clientResponseDTO = clientService.updateClient(inn, clientForUpdateDTO);
        return ResponseEntity.ok(clientResponseDTO);
    }


}
