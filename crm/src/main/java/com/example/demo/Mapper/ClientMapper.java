package com.example.demo.Mapper;

import com.example.demo.DTO.*;
import com.example.demo.Model.Client;
import com.example.demo.Model.User;

public interface ClientMapper {

    public ClientInfoResponseDTO toClientInfoResponseDTO(Client client);

    public Client toClient(CreateClientDTO createClientDTO);

    public ClientWithTasksDTO toClientAndHisTasksDTO(Client client);

    public ClientResponseDTO toClientResponseDTO(Client client);
}
