package com.example.HotelBookingSystem.repository;

import com.example.HotelBookingSystem.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setup() {
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        roleRepository.save(adminRole);

        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        roleRepository.save(userRole);
    }

    @Test
    void testFindByName_Found() {
        Optional<Role> role = roleRepository.findByName("ROLE_ADMIN");

        assertThat(role).isPresent();
        assertThat(role.get().getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void testFindByName_NotFound() {
        Optional<Role> role = roleRepository.findByName("ROLE_MANAGER");

        assertThat(role).isNotPresent();
    }

    @Test
    void testExistsByName_True() {
        boolean exists = roleRepository.existsByName("ROLE_USER");

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByName_False() {
        boolean exists = roleRepository.existsByName("ROLE_GUEST");

        assertThat(exists).isFalse();
    }
}
