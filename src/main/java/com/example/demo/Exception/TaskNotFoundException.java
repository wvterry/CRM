package com.example.demo.Exception;

public class TaskNotFoundException extends RuntimeException{
    public TaskNotFoundException(String massage){
        super(massage);
    }
}
