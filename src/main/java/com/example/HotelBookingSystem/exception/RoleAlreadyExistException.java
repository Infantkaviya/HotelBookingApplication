package com.example.HotelBookingSystem.exception;

public class RoleAlreadyExistException extends RuntimeException {

    public RoleAlreadyExistException(String message) {
        super(message);
    }

}