package com.example.HotelBookingSystem.exception;

public class InvalidBookingRequestException extends RuntimeException {

    public  InvalidBookingRequestException(String message) {
        super(message);
    }

}