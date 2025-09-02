package com.example.HotelBookingSystem.repository;

import com.example.HotelBookingSystem.entity.BookedRoom;
import com.example.HotelBookingSystem.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository; // assuming you have this repo

    private Room testRoom;

    @BeforeEach
    void setup() {
        // Create and save a room first
        testRoom = new Room();
        testRoom.setRoomType("Deluxe");
        testRoom.setRoomPrice(BigDecimal.valueOf(2000));
        testRoom = roomRepository.save(testRoom);

        // Create and save a booking
        BookedRoom booking = new BookedRoom();
        booking.setBookingConfirmationCode("CONF123");
        booking.setCheckInDate(LocalDate.now());
        booking.setCheckOutDate(LocalDate.now().plusDays(2));
        booking.setGuestEmail("guest@example.com");
        booking.setRoom(testRoom);

        bookingRepository.save(booking);
    }

    @Test
    void testFindByBookingConfirmationCode() {
        Optional<BookedRoom> found = bookingRepository.findByBookingConfirmationCode("CONF123");

        assertThat(found).isPresent();
        assertThat(found.get().getGuestEmail()).isEqualTo("guest@example.com");
    }

    @Test
    void testFindByRoomId() {
        List<BookedRoom> bookings = bookingRepository.findByRoomId(testRoom.getId());

        assertThat(bookings).isNotEmpty();
        assertThat(bookings.get(0).getRoom().getRoomType()).isEqualTo("Deluxe");
    }

    @Test
    void testFindByGuestEmail() {
        List<BookedRoom> bookings = bookingRepository.findByGuestEmail("guest@example.com");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getBookingConfirmationCode()).isEqualTo("CONF123");
    }
}
