package com.aleksandr.spring.firstRestApp.util;

public class PersonNotCreateException extends RuntimeException{
    public PersonNotCreateException(String msg) {
        super(msg);
    }
}
