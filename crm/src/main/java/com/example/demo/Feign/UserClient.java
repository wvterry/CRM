package com.example.demo.Feign;

import com.example.demo.DTO.CreateUserRequestDTO;
import com.example.demo.DTO.CreateUserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service",
        url = "${user.service.url}",
        path = "/api/user",
        configuration = FeignConfig.class)

public interface UserClient {

    @PostMapping
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    CreateUserResponseDTO createUser(@RequestBody CreateUserRequestDTO request);

    @DeleteMapping("/{id}")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    void deleteUser(@PathVariable("id") Long id);

}

