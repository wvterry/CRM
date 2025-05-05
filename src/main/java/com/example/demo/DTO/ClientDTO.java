package com.example.demo.DTO;


import com.example.demo.Enum.ClientType;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientDTO {

    private String inn;

    private String name;

    private ClientType clientType;
}
