package com.example.demo.Service;

import com.example.demo.DTO.*;
import com.example.demo.Exception.ClientNotFoundException;
import com.example.demo.Mapper.ClientMapper;
import com.example.demo.Model.Client;
import com.example.demo.Repository.ClientRepository;
import com.example.demo.Repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;


    @Autowired
    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper){
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    @Transactional(readOnly = true)
    public List<ClientInfoResponseDTO> getAllClients(){
        return clientRepository.findAll().stream().map(clientMapper::toClientInfoResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO getClientByInn(Long inn){
        Client client = clientRepository
                .findByInn(inn)
                .orElseThrow(() -> new ClientNotFoundException("Клиент с ИНН " + inn + " не найден"));
        return clientMapper.toClientResponseDTOFromClient(client);
    }

    @Transactional
    public Long saveClient(CreateClientDTO createClientDTO){
        clientRepository.save(clientMapper.toClient(createClientDTO));
        return createClientDTO.getInn();
    }

    @Transactional
    public void deleteClientByInn(Long inn){
        Optional<Client> clientForDelete = clientRepository.findByInn(inn);
        if (clientForDelete.isEmpty()){
            throw new ClientNotFoundException("Клиент с ИНН " + inn + " не найден");
        }
        clientRepository.deleteByInn(inn);
    }

    @Transactional(readOnly = true)
    public ClientWithTasksDTO getClientsAndHisTasks(Long inn){
        Client client = clientRepository.findByInn(inn).orElseThrow(() -> new ClientNotFoundException("Клиент с ИНН " + inn + " не найден"));
      return clientMapper.toClientAndHisTasksDTO(client);
    }

    @Transactional
    public ClientResponseDTO updateClient(Long inn, ClientForUpdateDTO clientForUpdateDTO){
        Client clientForUpdate = clientRepository
                .findByInn(inn)
                .orElseThrow(() -> new ClientNotFoundException("Клиент с ИНН " + inn + " не найден"));
        clientRepository.save(clientMapper.toClientFromClientForUpdateDTO(clientForUpdate, clientForUpdateDTO));
        return clientMapper.toClientResponseDTOFromClient(clientForUpdate);
    }
}
