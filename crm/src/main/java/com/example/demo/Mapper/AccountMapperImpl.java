package com.example.demo.Mapper;

import com.example.demo.DTO.AccountInfoDTO;
import com.example.demo.Model.Account;
import org.springframework.stereotype.Component;

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
