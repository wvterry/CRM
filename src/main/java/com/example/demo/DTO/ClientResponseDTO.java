package com.example.demo.DTO;

import com.example.demo.Enum.ClientType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientResponseDTO {

    private String name;

    private String phone;

    private String email;

    private String address;

    private ClientType clientType;
}
