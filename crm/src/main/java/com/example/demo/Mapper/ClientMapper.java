package com.example.demo.Mapper;

import com.example.demo.DTO.ClientInfoResponseDTO;
import com.example.demo.DTO.ClientResponseDTO;
import com.example.demo.DTO.ClientWithTasksDTO;
import com.example.demo.DTO.CreateClientDTO;
import com.example.demo.Model.Client;

public interface ClientMapper {
    ClientInfoResponseDTO toClientInfoResponseDTO(Client client);
    Client toClient(CreateClientDTO createClientDTO);
    ClientWithTasksDTO toClientAndHisTasksDTO(Client client);
    ClientResponseDTO toClientResponseDTO(Client client);
}
