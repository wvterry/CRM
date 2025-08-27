package com.example.demo.Service;

import com.example.demo.Constants.UserConstants;
import com.example.demo.DTO.*;
import com.example.demo.Exception.NotFoundException;
import com.example.demo.Mapper.ClientMapper;
import com.example.demo.Model.Account;
import com.example.demo.Model.Client;
import com.example.demo.Model.User;
import com.example.demo.Repository.AccountRepository;
import com.example.demo.Repository.ClientRepository;
import com.example.demo.Repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;


    @Autowired
    public ClientService(ClientRepository clientRepository,
                         ClientMapper clientMapper,
                         UserRepository userRepository,
                         AccountRepository accountRepository) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<ClientInfoResponseDTO> getAllClients() {
        return clientRepository.findAll().stream().map(clientMapper::toClientInfoResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO getClientByInn(Long inn) {
        Client client = clientRepository
                .findByInn(inn)
                .orElseThrow(() -> new NotFoundException("Клиент с ИНН " + inn + " не найден"));
        return clientMapper.toClientResponseDTO(client);
    }

    @Transactional
    public Long saveClient(String creatorEmail, CreateClientDTO createClientDTO) throws BadRequestException {

        if (clientRepository.findByInn(createClientDTO.getInn()).isPresent()) {
            throw new BadRequestException("Клиент с таким ИНН уже есть в системе");
        }
        Account account = accountRepository.findByEmail(creatorEmail).get();
        User user = userRepository.findById(account.getUser().getUserId()).get();

        Client client = clientMapper.toClient(createClientDTO);
        client.setManager(user);

        clientRepository.save(client);
        return createClientDTO.getInn();
    }

    @Transactional
    public void deleteClientByInn(Long inn) {
        Optional<Client> clientForDelete = clientRepository.findByInn(inn);
        if (clientForDelete.isEmpty()) {
            throw new NotFoundException("Клиент с ИНН " + inn + " не найден");
        }
        clientRepository.deleteByInn(inn);
    }

    @Transactional(readOnly = true)
    public ClientWithTasksDTO getClientsAndHisTasks(Long inn) {
        Client client = clientRepository.findByInn(inn).orElseThrow(
                () -> new NotFoundException("Клиент с ИНН " + inn + " не найден"));
        return clientMapper.toClientAndHisTasksDTO(client);
    }

    @Transactional
    public ClientResponseDTO updateClient(Long inn, ClientForUpdateDTO clientForUpdateDTO) {
        Client clientForUpdate = clientRepository
                .findByInn(inn)
                .orElseThrow(() -> new NotFoundException("Клиент с ИНН " + inn + " не найден"));

        clientForUpdate.setPhone(clientForUpdateDTO.getPhone());
        clientForUpdate.setEmail(clientForUpdateDTO.getEmail());
        clientForUpdate.setAddress(clientForUpdateDTO.getAddress());
        clientForUpdate.setName(clientForUpdateDTO.getName());

        clientRepository.save(clientForUpdate);
        return clientMapper.toClientResponseDTO(clientForUpdate);
    }

    @Transactional
    public void changeManagerHandler(Long userId){
        User manager = userRepository.findById(userId).get();
        User noNameUser = userRepository.findById(UserConstants.NO_NAME_USER_ID).get();
        List<Client> usersClients = clientRepository.findByManager(manager);
        usersClients.forEach(client -> {
            client.setManager(noNameUser);
            clientRepository.save(client);
        });
    }
}
