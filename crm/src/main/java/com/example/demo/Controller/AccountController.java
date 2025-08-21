package com.example.demo.Controller;

import com.example.demo.DTO.AccountInfoDTO;
import com.example.demo.DTO.UpdateEmailDTO;
import com.example.demo.JWT.JwtTokenService;
import com.example.demo.Service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.securitycommon.jwt.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final JwtUtil jwtUtil;
    private final AccountService accountService;

    @Autowired
    public AccountController(JwtUtil jwtUtil, AccountService accountService) {
        this.jwtUtil = jwtUtil;
        this.accountService = accountService;
    }

    @PutMapping("/updateemail")
    public ResponseEntity<AccountInfoDTO> updateEmail(HttpServletRequest httpServletRequest,
                                                      @RequestBody UpdateEmailDTO updateEmailDTO){
        String token = jwtUtil.getTokenFromRequest(httpServletRequest);
        String email = jwtUtil.getEmailFromToken(token);

        return ResponseEntity.ok(accountService.updateEmail(email, updateEmailDTO));
    }

    @GetMapping
    public ResponseEntity<List<AccountInfoDTO>> getAllAccounts(){
        return ResponseEntity.ok(accountService.getAll());
    }

}
