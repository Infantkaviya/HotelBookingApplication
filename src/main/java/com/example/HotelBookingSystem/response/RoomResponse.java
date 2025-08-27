package com.example.HotelBookingSystem.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.apache.tomcat.util.codec.binary.Base64;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Getter
@Setter
public class RoomResponse {


    private long id;

    private String roomType;

    private BigDecimal roomPrice;

    private boolean isBooked = false;

    private String photo;

    private List<BookingResponse> bookings;

    public long getId() {
        return id;
    }
    public String getRoomType() {
        return roomType;
    }
    public BigDecimal getRoomPrice() {
        return roomPrice;
    }

    public RoomResponse(Long id, String roomType, BigDecimal roomPrice) {
        this.id = id;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
    }


    public RoomResponse(Long id, String roomType, BigDecimal roomPrice, boolean isBooked,
                        byte[] photoBytes , List<BookingResponse> bookings) {
        super();
        this.id = id;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.isBooked = isBooked;
        this.photo = photoBytes != null?Base64.encodeBase64String(photoBytes):null;
        this.bookings = bookings;
    }
}