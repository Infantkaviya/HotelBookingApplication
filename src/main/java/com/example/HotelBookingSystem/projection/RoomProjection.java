package com.example.HotelBookingSystem.projection;

import java.math.BigDecimal;

public interface RoomProjection {
    Long getId();
    BigDecimal getRoomPrice();
    String getRoomType();
    byte[] getPhoto();
    boolean getIsBooked();
}
