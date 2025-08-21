package com.example.demo.Mapper;

import com.example.demo.DTO.ClientInfoResponseDTO;
import com.example.demo.DTO.ClientResponseDTO;
import com.example.demo.DTO.ClientWithTasksDTO;
import com.example.demo.DTO.CreateClientDTO;
import com.example.demo.Model.Client;

public interface ClientMapper {
    public ClientInfoResponseDTO toClientInfoResponseDTO(Client client);
    public Client toClient(CreateClientDTO createClientDTO);
    public ClientWithTasksDTO toClientAndHisTasksDTO(Client client);
    public ClientResponseDTO toClientResponseDTO(Client client);
}
