package com.example.HotelBookingSystem.service;

import com.example.HotelBookingSystem.entity.Role;
import com.example.HotelBookingSystem.entity.User;

import java.util.List;

public interface IRoleService {
    List<Role> getRoles();

    Role createRole(Role theRole);

    void deleteRole(Long id);

    Role findByName(String name);

    User removeUserFromRole(Long userId,Long roleId);

    User assignRoleToUser(Long userId, Long roleId);

    Role removeAllUserFromRole(Long roleId);

}