package com.example.demo.Mapper;

import com.example.demo.DTO.AccountInfoDTO;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Model.Account;
import com.example.demo.Model.User;

import java.util.Set;

public interface AccountMapper {

    public AccountInfoDTO toAccountInfoDTO(Account account);
}
