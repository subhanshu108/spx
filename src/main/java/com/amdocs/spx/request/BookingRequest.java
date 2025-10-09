package com.amdocs.spx.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private Long bookingId;
    private Long userId;
    private Long eventId;
    private Long ticketTypeId;
    private int quantity;
    private String bookingReference;
    private String bookingStatus;
    private String eventName;
    private LocalDate bookingDate;
}
