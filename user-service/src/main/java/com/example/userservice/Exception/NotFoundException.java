package com.example.userservice.Exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String massage) {
        super(massage, null, false, false);
    }
}
