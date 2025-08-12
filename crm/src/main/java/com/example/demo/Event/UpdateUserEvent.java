package com.example.demo.Event;

import com.example.demo.DTO.UpdateUserDTO;

public record UpdateUserEvent(String type,
                              Long userId,
                              UpdateUserDTO updateUserDTO) {
}
