package com.amdocs.spx.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private Long userId;
    private Long eventId;
    private Long ticketTypeId;
    private int quantity;
    private String bookingReference;
    private String bookingStatus;
}
