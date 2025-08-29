package com.example.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountInfoDTO {

    private Long accountId;

    private String email;

    private String userFirstName;

    private String userLastName;
}
