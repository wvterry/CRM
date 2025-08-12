package com.example.userservice.Event;

public record DeleteUserEvent(String type,
                              Long userId) {
}
