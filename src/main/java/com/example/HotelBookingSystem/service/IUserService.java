package com.example.HotelBookingSystem.service;

import com.example.HotelBookingSystem.entity.User;

import java.util.List;

public interface IUserService {

    User registerUser(User user);
    List<User> getUsers();
    void deleteUser(String email);
    User getUser(String email);

}