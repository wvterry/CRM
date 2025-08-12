package com.example.demo.Mapper;

import com.example.demo.DTO.*;
import com.example.demo.Model.Client;
import com.example.demo.Model.User;

public interface ClientMapper {

    public ClientInfoResponseDTO toClientInfoResponseDTO(Client client);

    public Client toClient(User user, CreateClientDTO createClientDTO);

    public ClientWithTasksDTO toClientAndHisTasksDTO(Client client);

    public Client toClient(Client clientForUpdate, ClientForUpdateDTO clientForUpdateDTO);

    public ClientResponseDTO toClientResponseDTO(Client client);
}
