package com.example.demo.Mapper;

import com.example.demo.DTO.AccountInfoDTO;
import com.example.demo.Model.Account;

public interface AccountMapper {

    AccountInfoDTO toAccountInfoDTO(Account account);
}
