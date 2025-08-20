package com.example.demo.JWT;

import com.example.demo.DTO.UpdateAccountRoleDTO;
import com.example.demo.DTO.UpdatePasswordDTO;
import com.example.demo.Service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accountService;

    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AccountService accountService, JwtUtil jwtUtil) {
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signin")
    public String authenticateUser(@RequestBody AuthRequest authRequest){
       return accountService.authenticate(authRequest);
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> registerUser(@RequestBody SignupRequest signupRequest) throws BadRequestException {
        accountService.register(signupRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/updatepass")
    public ResponseEntity<Void> updatePassword(HttpServletRequest httpServletRequest,
                                               @RequestBody UpdatePasswordDTO updatePasswordDTO) throws BadRequestException{
        String token = jwtUtil.getTokenFromRequest(httpServletRequest);
        String email = jwtUtil.getEmailFromToken(token);
        accountService.updatePass(email, updatePasswordDTO);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/updaterole/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateAccountRole(@PathVariable("id") Long accountId,
                                                  @RequestBody UpdateAccountRoleDTO updateAccountRoleDTO){
        accountService.updateRole(accountId, updateAccountRoleDTO);
        return ResponseEntity.ok().build();
    }

}
