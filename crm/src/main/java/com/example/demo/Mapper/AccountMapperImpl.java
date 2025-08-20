package com.example.demo.Mapper;

import com.example.demo.DTO.AccountInfoDTO;
import com.example.demo.JWT.SignupRequest;
import com.example.demo.Model.Account;
import com.example.demo.Model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class AccountMapperImpl implements AccountMapper{

    @Override
    public AccountInfoDTO toAccountInfoDTO(Account account) {
        AccountInfoDTO accountInfoDTO = new AccountInfoDTO();
        accountInfoDTO.setUserFirstName(account.getUser().getFirstName());
        accountInfoDTO.setUserLastName(account.getUser().getLastName());
        accountInfoDTO.setAccountId(account.getAccountId());
        accountInfoDTO.setEmail(account.getEmail());
        return accountInfoDTO;
    }
}
