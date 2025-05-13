package com.example.demo.Exception;

public class TaskForUpdateNotFoundException extends RuntimeException{
    public TaskForUpdateNotFoundException(String massage){
        super(massage);
    }
}
