package com.example.demo.JWT;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.antlr.v4.runtime.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {


    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int expiration;

    private SecretKey key;

    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, Collection<? extends GrantedAuthority> authorities){
        return Jwts.builder()
                .setSubject(email)
                .claim("authorities", authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public List<GrantedAuthority> getAuthorityFromToken(String token){

        List<String> authorities = Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .get("authorities", List.class);

        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public String getEmailFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getTokenFromRequest(HttpServletRequest httpServletRequest){
        String token = httpServletRequest.getHeader("Authorization");
        if (token!= null && token.startsWith("Bearer ")){
            return token.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException e){
            System.out.println("Invalid JWT signature: " + e.getMessage());  //Заменить везде System.out.println
        } catch (MalformedJwtException e){
            System.out.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e){
            System.out.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e){
            System.out.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e){
            System.out.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }
}
