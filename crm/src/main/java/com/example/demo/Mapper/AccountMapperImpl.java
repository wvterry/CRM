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
    public Account toAccount(User user, Long userId, Set roles, String password, SignupRequest signupRequest) {
        Account account = new Account();
        account.setEmail(signupRequest.getEmail());
        account.setPassword(password);
        account.setRoles(roles);
        account.setCreatedAt(LocalDateTime.now());
//        account.setUserId(userId);
        account.setUser(user);
        return account;
    }

    @Override
    public Account toAccount(String password, Account account) {
        account.setPassword(password);
        return account;
    }

    @Override
    public AccountInfoDTO toAccountInfoDTO(Account account, User user) {
        AccountInfoDTO accountInfoDTO = new AccountInfoDTO();
        accountInfoDTO.setUserFirstName(user.getFirstName());
        accountInfoDTO.setUserLastName(user.getLastName());
        accountInfoDTO.setAccountId(account.getAccountId());
        accountInfoDTO.setEmail(account.getEmail());
        return accountInfoDTO;
    }
}
