package com.example.HotelBookingSystem.service;

import com.example.HotelBookingSystem.entity.BookedRoom;
import com.example.HotelBookingSystem.entity.Room;
import com.example.HotelBookingSystem.exception.InvalidBookingRequestException;
import com.example.HotelBookingSystem.exception.ResourceNotFoundException;
import com.example.HotelBookingSystem.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private IRoomService roomService;

    @InjectMocks
    private BookingService bookingService;

    private Room room;
    private BookedRoom booking;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        room = new Room();
        room.setId(1L);

        booking = new BookedRoom();
        booking.setBookingId(1L);
        booking.setCheckInDate(LocalDate.of(2025, 9, 10));
        booking.setCheckOutDate(LocalDate.of(2025, 9, 15));
        booking.setBookingConfirmationCode("CONF123");
        booking.setGuestEmail("guest@example.com");
    }

    @Test
    void testGetAllBookings() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        List<BookedRoom> result = bookingService.getAllBookings();

        assertThat(result).hasSize(1);
        verify(bookingRepository, times(1)).findAll();
    }

    @Test
    void testGetBookingsByUserEmail() {
        when(bookingRepository.findByGuestEmail("guest@example.com")).thenReturn(List.of(booking));

        List<BookedRoom> result = bookingService.getBookingsByUserEmail("guest@example.com");

        assertThat(result).contains(booking);
        verify(bookingRepository, times(1)).findByGuestEmail("guest@example.com");
    }

    @Test
    void testCancelBooking() {
        bookingService.cancelBooking(1L);

        verify(bookingRepository, times(1)).deleteById(1L);
    }

    @Test
    void testSaveBooking_Success() {
        room.setBookings(List.of()); // No existing bookings

        when(roomService.getRoomById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.save(any(BookedRoom.class))).thenReturn(booking);

        String confirmationCode = bookingService.saveBooking(1L, booking);

        assertThat(confirmationCode).isEqualTo("CONF123");
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void testSaveBooking_InvalidDates_ShouldThrowException() {
        booking.setCheckInDate(LocalDate.of(2025, 9, 20));
        booking.setCheckOutDate(LocalDate.of(2025, 9, 15));

        assertThatThrownBy(() -> bookingService.saveBooking(1L, booking))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessageContaining("Check-in date must come before check-out date");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testSaveBooking_RoomNotAvailable_ShouldThrowException() {
        BookedRoom existingBooking = new BookedRoom();
        existingBooking.setCheckInDate(LocalDate.of(2025, 9, 10));
        existingBooking.setCheckOutDate(LocalDate.of(2025, 9, 15));
        room.setBookings(List.of(existingBooking));

        when(roomService.getRoomById(1L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> bookingService.saveBooking(1L, booking))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessageContaining("not available");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void testFindByBookingConfirmationCode_Success() {
        when(bookingRepository.findByBookingConfirmationCode("CONF123")).thenReturn(Optional.of(booking));

        BookedRoom result = bookingService.findByBookingConfirmationCode("CONF123");

        assertThat(result).isEqualTo(booking);
    }

    @Test
    void testFindByBookingConfirmationCode_NotFound_ShouldThrowException() {
        when(bookingRepository.findByBookingConfirmationCode("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.findByBookingConfirmationCode("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No booking found");

        verify(bookingRepository, times(1)).findByBookingConfirmationCode("INVALID");
    }
}
