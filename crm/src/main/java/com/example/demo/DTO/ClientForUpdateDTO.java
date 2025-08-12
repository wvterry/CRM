package com.example.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientForUpdateDTO {

    private String name;

    private String phone;

    private String email;

    private String address;
}
