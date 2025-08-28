package com.example.HotelBookingSystem.repository;

import com.example.HotelBookingSystem.entity.Room;
import com.example.HotelBookingSystem.projection.RoomProjection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT DISTINCT r.roomType FROM Room r")
    List<String> findDistinctRoomTypes();

    @Query(" SELECT r FROM Room r " +
            " WHERE r.roomType LIKE %:roomType% " +
            " AND r.id NOT IN (" +
            "  SELECT br.room.id FROM BookedRoom br " +
            "  WHERE ((br.checkInDate <= :checkOutDate) AND (br.checkOutDate >= :checkInDate))" +
            ")")

    List<Room> findAvailableRoomsByDatesAndType(LocalDate checkInDate, LocalDate checkOutDate, String roomType);

    @Query("SELECT r.id AS id, r.roomType AS roomType, r.roomPrice AS roomPrice, r.photo AS photo, r.isBooked AS isBooked " +
       "FROM Room r " +
       "WHERE r.roomType LIKE %:roomType% " +
       "AND r.id NOT IN (" +
       "  SELECT br.room.id FROM BookedRoom br " +
       "  WHERE (br.checkInDate <= :checkOutDate AND br.checkOutDate >= :checkInDate)" +
       ")")
    List<RoomProjection> findAvailableRoomProjections(
        @Param("checkInDate") LocalDate checkInDate,
        @Param("checkOutDate") LocalDate checkOutDate,
        @Param("roomType") String roomType);

}