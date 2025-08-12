package com.example.demo.Feign;

import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class FeignConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${internal.api.key}")
    private String apiKey;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public RequestInterceptor apiKeyInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-API-KEY", apiKey);
            requestTemplate.header("Content-Type", "application/json");
        };
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(1000, 5000, 3);
    }

    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(5000, 5000);
    }
}
