package com.example.HotelBookingSystem.service;

import com.example.HotelBookingSystem.entity.Room;
import com.example.HotelBookingSystem.exception.InternalServerException;
import com.example.HotelBookingSystem.exception.ResourceNotFoundException;
import com.example.HotelBookingSystem.repository.RoomRepository;
import com.example.HotelBookingSystem.response.RoomResponse;
import com.example.HotelBookingSystem.projection.RoomProjection;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService implements IRoomService{

    private final RoomRepository roomRepository;
    @Override
    public Room addNewRoom(MultipartFile file, String roomType, BigDecimal roomPrice) throws SerialException, SQLException, IOException {
        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);
        if(!file.isEmpty()) {
           room.setPhoto(file.getBytes());
        }

        return roomRepository.save(room);
    }
    @Override
    public List<String> getAllRoomTypes() {

        return roomRepository.findDistinctRoomTypes();
    }
    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
    @Override
    public byte[] getRoomPhotoByRoomId(Long roomId) throws SQLException {
        Optional<Room> theRoom = roomRepository.findById(roomId);
        if(theRoom.isEmpty()) {
            throw new ResourceNotFoundException("Sorry, Room not found!");
        }
        byte[] photoBlob=theRoom.get().getPhoto();
        if(photoBlob != null) {
            return photoBlob;
        }
        return null;
    }
    @Override
    public void deleteRoom(Long roomId) {
        Optional<Room> theRoom = roomRepository.findById(roomId);
        if (theRoom.isPresent()) {
            roomRepository.deleteById(roomId);
        }
    }
    @Override
    public Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice, byte[] photoBytes) {
        Room room = roomRepository.findById(roomId).orElseThrow(()-> new ResourceNotFoundException("Room not found"));
        if(roomType != null) {
            room.setRoomType(roomType);
        }
        if(roomPrice!= null) {
            room.setRoomPrice(roomPrice);
        }
        if(photoBytes!=null && photoBytes.length>0) {
                room.setPhoto(photoBytes);
        }
        return roomRepository.save(room);
    }
    @Override
    public Optional<Room> getRoomById(Long roomId) {
        return Optional.of(roomRepository.findById(roomId).get());
    }
    @Override
    public List<Room> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, String roomType) {
        return roomRepository.findAvailableRoomsByDatesAndType(checkInDate, checkOutDate, roomType);
    }

    @Transactional
    public List<Room> getAvailableRoomsDto(LocalDate checkIn, LocalDate checkOut, String roomType) {
        List<RoomProjection> projections = roomRepository.findAvailableRoomProjections(checkIn, checkOut, roomType);

        return projections.stream().map(proj -> {
            Room response = new Room();
            response.setId(proj.getId());
            response.setRoomType(proj.getRoomType());
            response.setRoomPrice(proj.getRoomPrice());
            response.setBooked(proj.getIsBooked());

            if (proj.getPhoto() != null) {
                response.setPhoto(proj.getPhoto());
            }
            return response;
        }).toList();
    }
}