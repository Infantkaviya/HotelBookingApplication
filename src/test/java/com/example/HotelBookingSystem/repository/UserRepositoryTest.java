package com.example.HotelBookingSystem.repository;

import com.example.HotelBookingSystem.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setFirstName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("password123");
        userRepository.save(testUser);
    }

    @Test
    void testExistsByEmail_ShouldReturnTrue() {
        boolean exists = userRepository.existsByEmail("john@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByEmail_ShouldReturnFalse() {
        boolean exists = userRepository.existsByEmail("jane@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void testFindByEmail_ShouldReturnUser() {
        Optional<User> userOpt = userRepository.findByEmail("john@example.com");

        assertThat(userOpt).isPresent();
        assertThat(userOpt.get().getFirstName()).isEqualTo("John Doe");
    }

    @Test
    void testFindByEmail_ShouldReturnEmpty() {
        Optional<User> userOpt = userRepository.findByEmail("unknown@example.com");

        assertThat(userOpt).isNotPresent();
    }

    @Test
    void testDeleteByEmail_ShouldRemoveUser() {
        userRepository.deleteByEmail("john@example.com");

        Optional<User> userOpt = userRepository.findByEmail("john@example.com");

        assertThat(userOpt).isEmpty();
    }
}
