package com.example.HotelBookingSystem.controller;

import com.example.HotelBookingSystem.entity.BookedRoom;
import com.example.HotelBookingSystem.entity.Room;
import com.example.HotelBookingSystem.exception.InvalidBookingRequestException;
import com.example.HotelBookingSystem.exception.ResourceNotFoundException;
import com.example.HotelBookingSystem.response.BookingResponse;
import com.example.HotelBookingSystem.response.RoomResponse;
import com.example.HotelBookingSystem.service.IBookingService;
import com.example.HotelBookingSystem.service.IRoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IBookingService bookingService;

    @MockBean
    private IRoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookedRoom bookedRoom;
    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room();
        room.setId(1L);
        room.setRoomType("DELUXE");
        room.setRoomPrice(200.0);

        bookedRoom = new BookedRoom();
        bookedRoom.setBookingId(1L);
        bookedRoom.setGuestFullName("John Doe");
        bookedRoom.setGuestEmail("john@example.com");
        bookedRoom.setCheckInDate(LocalDate.now());
        bookedRoom.setCheckOutDate(LocalDate.now().plusDays(2));
        bookedRoom.setNumOfAdults(2);
        bookedRoom.setNumOfChildren(1);
        bookedRoom.setTotalNumOfGuest(3);
        bookedRoom.setBookingConfirmationCode("CONF123");
        bookedRoom.setRoom(room);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllBookings() throws Exception {
        Mockito.when(bookingService.getAllBookings()).thenReturn(List.of(bookedRoom));
        Mockito.when(roomService.getRoomById(1L)).thenReturn(Optional.of(room));

        mockMvc.perform(get("/bookings/all-bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].guestFullName").value("John Doe"))
                .andExpect(jsonPath("$[0].room.roomType").value("DELUXE"));
    }

    @Test
    void testSaveBookingSuccess() throws Exception {
        Mockito.when(bookingService.saveBooking(eq(1L), any(BookedRoom.class)))
                .thenReturn("CONF123");

        mockMvc.perform(post("/bookings/room/{roomId}/booking", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookedRoom)))
                .andExpect(status().isOk())
                .andExpect(content().string("Room booked successfully, Your booking confirmation code is :CONF123"));
    }

    @Test
    void testSaveBookingInvalidRequest() throws Exception {
        Mockito.when(bookingService.saveBooking(eq(1L), any(BookedRoom.class)))
                .thenThrow(new InvalidBookingRequestException("Invalid booking request"));

        mockMvc.perform(post("/bookings/room/{roomId}/booking", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookedRoom)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid booking request"));
    }

    @Test
    void testGetBookingByConfirmationCodeSuccess() throws Exception {
        Mockito.when(bookingService.findByBookingConfirmationCode("CONF123"))
                .thenReturn(bookedRoom);
        Mockito.when(roomService.getRoomById(1L)).thenReturn(Optional.of(room));

        mockMvc.perform(get("/bookings/confirmation/{confirmationCode}", "CONF123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestFullName").value("John Doe"))
                .andExpect(jsonPath("$.room.roomType").value("DELUXE"));
    }

    @Test
    void testGetBookingByConfirmationCodeNotFound() throws Exception {
        Mockito.when(bookingService.findByBookingConfirmationCode("CONF999"))
                .thenThrow(new ResourceNotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/confirmation/{confirmationCode}", "CONF999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Booking not found"));
    }

    @Test
    void testGetBookingsByUserEmail() throws Exception {
        Mockito.when(bookingService.getBookingsByUserEmail("john@example.com"))
                .thenReturn(List.of(bookedRoom));
        Mockito.when(roomService.getRoomById(1L)).thenReturn(Optional.of(room));

        mockMvc.perform(get("/bookings/user/{email}/bookings", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].guestEmail").value("john@example.com"))
                .andExpect(jsonPath("$[0].room.roomType").value("DELUXE"));
    }

    @Test
    void testCancelBooking() throws Exception {
        Mockito.doNothing().when(bookingService).cancelBooking(1L);

        mockMvc.perform(delete("/bookings/booking/{bookingId}/delete", 1L))
                .andExpect(status().isOk());
    }
}
