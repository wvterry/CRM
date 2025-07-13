package com.example.demo.JWT;

import com.example.demo.Service.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signin")
    public String authenticateUser(@RequestBody AuthRequest authRequest){
       return userService.authenticate(authRequest);
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> registerUser(@RequestBody SignupRequest signupRequest) throws BadRequestException {
        userService.register(signupRequest);
        return ResponseEntity.ok().build();
    }
}
