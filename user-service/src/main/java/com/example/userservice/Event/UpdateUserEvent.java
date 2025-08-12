package com.example.userservice.Event;

import com.example.userservice.DTO.UpdateUserDTO;

public record UpdateUserEvent(String type,
                              Long userId,
                              UpdateUserDTO updateUserDTO) {
}
