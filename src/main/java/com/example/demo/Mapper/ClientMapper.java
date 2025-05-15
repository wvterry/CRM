package com.example.demo.Mapper;

import com.example.demo.DTO.*;
import com.example.demo.Model.Client;

public interface ClientMapper {

    public ClientInfoResponseDTO toClientInfoResponseDTO(Client client);

    public Client toClient(CreateClientDTO createClientDTO);

    public ClientWithTasksDTO toClientAndHisTasksDTO(Client client);

    public Client toClientFromClientForUpdateDTO(Client clientForUpdate, ClientForUpdateDTO clientForUpdateDTO);

    public ClientResponseDTO toClientResponseDTOFromClient(Client client);
}
