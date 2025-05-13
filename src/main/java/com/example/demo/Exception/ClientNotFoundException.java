package com.example.demo.Exception;

public class ClientNotFoundException extends RuntimeException{

    public ClientNotFoundException(String massage){
        super(massage);
    }
}
