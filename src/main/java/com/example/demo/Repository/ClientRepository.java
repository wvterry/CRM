package com.example.demo.Repository;

import com.example.demo.DTO.ClientWithTasksDTO;
import com.example.demo.Model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByInn(Long inn);

    void deleteByInn(Long inn);

}
