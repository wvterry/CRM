package com.example.demo.JWT;

import com.example.demo.Exception.NotFoundException;
import com.example.demo.Model.Account;
import com.example.demo.Repository.AccountRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenService {

    private final int expiration;
    private final SecretKey key;
    private final AccountRepository accountRepository;

    @Autowired
    public JwtTokenService(@Value("${jwt.secret}") String jwtSecret,
                           @Value("${jwt.expiration}") int expiration,
                           AccountRepository accountRepository) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.accountRepository = accountRepository;
    }

    public String generateToken(String email, Collection<? extends GrantedAuthority> authorities) {
        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("Пользователь с email " + email + " не найден"));

        return Jwts.builder()
                .setSubject(email)
                .claim("authorities", authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .claim("userId", account.getUser().getUserId())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}
