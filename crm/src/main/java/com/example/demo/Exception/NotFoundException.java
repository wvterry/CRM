package com.example.demo.Exception;

public class NotFoundException extends RuntimeException{

    public NotFoundException(String massage){
        super(massage);
    }
}
