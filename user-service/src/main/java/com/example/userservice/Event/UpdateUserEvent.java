package com.example.userservice.Event;

import com.example.userservice.DTO.UpdateUserDTO;
import lombok.Builder;

@Builder
public record UpdateUserEvent(
        Long userId,
        UpdateUserDTO updateUserDTO) {
}
