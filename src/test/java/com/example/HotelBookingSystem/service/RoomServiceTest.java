package com.example.HotelBookingSystem.service;

import com.example.HotelBookingSystem.entity.Room;
import com.example.HotelBookingSystem.exception.ResourceNotFoundException;
import com.example.HotelBookingSystem.projection.RoomProjection;
import com.example.HotelBookingSystem.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private Room room;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        room = new Room();
        room.setId(1L);
        room.setRoomType("Deluxe");
        room.setRoomPrice(BigDecimal.valueOf(1500));
        room.setPhoto("photo".getBytes());
    }

    @Test
    void testAddNewRoom() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "room.jpg", "image/jpeg", "dummy".getBytes());
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        Room savedRoom = roomService.addNewRoom(file, "Deluxe", BigDecimal.valueOf(1500));

        assertNotNull(savedRoom);
        assertEquals("Deluxe", savedRoom.getRoomType());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void testGetAllRoomTypes() {
        when(roomRepository.findDistinctRoomTypes()).thenReturn(List.of("Deluxe", "Suite"));

        List<String> types = roomService.getAllRoomTypes();

        assertEquals(2, types.size());
        verify(roomRepository, times(1)).findDistinctRoomTypes();
    }

    @Test
    void testGetAllRooms() {
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<Room> rooms = roomService.getAllRooms();

        assertEquals(1, rooms.size());
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    void testGetRoomPhotoByRoomId_Success() throws SQLException {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        byte[] photo = roomService.getRoomPhotoByRoomId(1L);

        assertNotNull(photo);
        assertArrayEquals("photo".getBytes(), photo);
    }

    @Test
    void testGetRoomPhotoByRoomId_NotFound() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roomService.getRoomPhotoByRoomId(1L));
    }

    @Test
    void testDeleteRoom_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        roomService.deleteRoom(1L);

        verify(roomRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteRoom_NotFound() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        roomService.deleteRoom(1L);

        verify(roomRepository, never()).deleteById(anyLong());
    }

    @Test
    void testUpdateRoom_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        Room updatedRoom = roomService.updateRoom(1L, "Suite", BigDecimal.valueOf(2000), "new".getBytes());

        assertEquals("Suite", updatedRoom.getRoomType());
        assertEquals(BigDecimal.valueOf(2000), updatedRoom.getRoomPrice());
        assertArrayEquals("new".getBytes(), updatedRoom.getPhoto());
    }

    @Test
    void testUpdateRoom_NotFound() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            roomService.updateRoom(1L, "Suite", BigDecimal.valueOf(2000), null));
    }

    @Test
    void testGetRoomById_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        Optional<Room> found = roomService.getRoomById(1L);

        assertTrue(found.isPresent());
        assertEquals("Deluxe", found.get().getRoomType());
    }

    @Test
    void testGetAvailableRooms() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(2);
        when(roomRepository.findAvailableRoomsByDatesAndType(checkIn, checkOut, "Deluxe"))
                .thenReturn(List.of(room));

        List<Room> availableRooms = roomService.getAvailableRooms(checkIn, checkOut, "Deluxe");

        assertEquals(1, availableRooms.size());
        assertEquals("Deluxe", availableRooms.get(0).getRoomType());
    }

    @Test
    void testGetAvailableRoomsDto() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(2);

        RoomProjection projection = new RoomProjection() {
            @Override public Long getId() { return 1L; }
            @Override public String getRoomType() { return "Deluxe"; }
            @Override public BigDecimal getRoomPrice() { return BigDecimal.valueOf(1500); }
            @Override public boolean getIsBooked() { return false; }
            @Override public byte[] getPhoto() { return "photo".getBytes(); }
        };

        when(roomRepository.findAvailableRoomProjections(checkIn, checkOut, "Deluxe"))
                .thenReturn(List.of(projection));

        List<Room> result = roomService.getAvailableRoomsDto(checkIn, checkOut, "Deluxe");

        assertEquals(1, result.size());
        assertEquals("Deluxe", result.get(0).getRoomType());
    }
}
