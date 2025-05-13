package com.example.demo.DTO;

import com.example.demo.Enum.ClientType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateClientDTO {

    private Long inn;

    private String name;

    private ClientType clientType;
}
