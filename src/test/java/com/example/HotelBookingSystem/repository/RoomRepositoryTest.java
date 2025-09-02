package com.example.HotelBookingSystem.repository;

import com.example.HotelBookingSystem.entity.BookedRoom;
import com.example.HotelBookingSystem.entity.Room;
import com.example.HotelBookingSystem.projection.RoomProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Room deluxeRoom;
    private Room suiteRoom;

    @BeforeEach
    void setup() {
        // Save a Deluxe room
        deluxeRoom = new Room();
        deluxeRoom.setRoomType("Deluxe");
        deluxeRoom.setRoomPrice(BigDecimal.valueOf(2000));
        deluxeRoom.setBooked(false);
        roomRepository.save(deluxeRoom);

        // Save a Suite room
        suiteRoom = new Room();
        suiteRoom.setRoomType("Suite");
        suiteRoom.setRoomPrice(BigDecimal.valueOf(3000));
        suiteRoom.setBooked(false);
        roomRepository.save(suiteRoom);

        // Book the Deluxe room for a date range
        BookedRoom booking = new BookedRoom();
        booking.setRoom(deluxeRoom);
        booking.setCheckInDate(LocalDate.now());
        booking.setCheckOutDate(LocalDate.now().plusDays(3));
        booking.setGuestEmail("guest@example.com");
        booking.setBookingConfirmationCode("CONF123");
        bookingRepository.save(booking);
    }

    @Test
    void testFindDistinctRoomTypes() {
        List<String> roomTypes = roomRepository.findDistinctRoomTypes();

        assertThat(roomTypes).contains("Deluxe", "Suite");
    }

    @Test
    void testFindAvailableRoomsByDatesAndType_ShouldExcludeBookedRooms() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        List<Room> availableRooms = roomRepository.findAvailableRoomsByDatesAndType(checkIn, checkOut, "Deluxe");

        assertThat(availableRooms).isEmpty(); // Deluxe is booked for these dates
    }

    @Test
    void testFindAvailableRoomsByDatesAndType_ShouldReturnAvailableRoom() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(6);

        List<Room> availableRooms = roomRepository.findAvailableRoomsByDatesAndType(checkIn, checkOut, "Deluxe");

        assertThat(availableRooms).hasSize(1);
        assertThat(availableRooms.get(0).getRoomType()).isEqualTo("Deluxe");
    }

    @Test
    void testFindAvailableRoomProjections() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(6);

        List<RoomProjection> projections = roomRepository.findAvailableRoomProjections(checkIn, checkOut, "Suite");

        assertThat(projections).hasSize(1);
        RoomProjection suiteProjection = projections.get(0);

        assertThat(suiteProjection.getRoomType()).isEqualTo("Suite");
        assertThat(suiteProjection.getRoomPrice()).isEqualByComparingTo(BigDecimal.valueOf(3000));
    }
}
