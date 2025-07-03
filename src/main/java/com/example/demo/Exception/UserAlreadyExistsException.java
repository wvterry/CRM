package com.example.demo.Exception;

public class UserAlreadyExistsException extends RuntimeException{

    public UserAlreadyExistsException(String massage){
        super(massage);
    }
}
