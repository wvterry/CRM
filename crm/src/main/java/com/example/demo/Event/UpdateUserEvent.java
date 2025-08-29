package com.example.demo.Event;

import com.example.demo.DTO.UpdateUserDTO;

public record UpdateUserEvent(Long userId,
                              UpdateUserDTO updateUserDTO) {
}
