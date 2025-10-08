package com.amdocs.spx.controller;

import com.amdocs.spx.entity.Booking;
import com.amdocs.spx.entity.Event;
import com.amdocs.spx.entity.TicketType;
import com.amdocs.spx.entity.User;
import com.amdocs.spx.repository.EventRepository;
import com.amdocs.spx.repository.TicketTypeRepository;
import com.amdocs.spx.repository.UserRepository;
import com.amdocs.spx.request.BookingRequest;
import com.amdocs.spx.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private EventRepository eventRepository;


    private Booking convertToDto(BookingRequest bookingRequest) {
        Booking booking = new Booking();
        booking.setBookingReference(bookingRequest.getBookingReference());
        booking.setBookingDate(LocalDateTime.now());
        Event event = eventRepository.findById(bookingRequest.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));
        booking.setEvent(event);
        User user = userRepository.findById(bookingRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        booking.setUser(user);
        TicketType ticketType = ticketTypeRepository.findById(bookingRequest.getTicketTypeId())
                        .orElseThrow(() -> new RuntimeException("TicketType not found"));
        booking.setTicketType(ticketType);
        booking.setQuantity(bookingRequest.getQuantity());
        booking.setBookingStatus(bookingRequest.getBookingStatus());
        return booking;

    }

    /**
     * Create new booking
     */
    @PostMapping(value = "/createBooking", consumes = "application/json", produces = "application/json")
    public BookingRequest createBooking(@RequestBody BookingRequest bookingrequest) {
        Booking booking =  convertToDto(bookingrequest);
         bookingService.createBooking(booking);
         return bookingrequest;
    }

    /**
     * Get booking details
     */
    @GetMapping("/{bookingId}")
    public BookingRequest getBookingById(@PathVariable Long bookingId) {
       return bookingService.getBookingById(bookingId);
    }

    /**
     * Find booking by reference number
     */
    @PostMapping("/reference")
    public ResponseEntity<Booking> getBookingByReference(@RequestBody BookingReferenceRequest request) {
        try {
            Booking booking = bookingService.getBookingByReference(request.getBookingReference());
            return new ResponseEntity<>(booking, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all bookings for a user
     */
    @PostMapping("/user")
    public ResponseEntity<List<Booking>> getUserBookings(@RequestBody UserRequest request) {
        try {
            List<Booking> bookings = bookingService.getUserBookings(request.getUserId());
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all bookings for an event
     */
    @PostMapping("/event")
    public ResponseEntity<List<Booking>> getEventBookings(@RequestBody EventRequest request) {
        try {
            List<Booking> bookings = bookingService.getEventBookings(request.getEventId());
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Change booking status
     */
    @PutMapping("/{bookingId}/status")
    public ResponseEntity<Booking> updateBookingStatus(@PathVariable Long bookingId, @RequestBody StatusRequest request) {
        try {
            Booking updatedBooking = bookingService.updateBookingStatus(bookingId, request.getStatus());
            return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cancel a booking
     */
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long bookingId) {
        try {
            Booking cancelledBooking = bookingService.cancelBooking(bookingId);
            return new ResponseEntity<>(cancelledBooking, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Confirm booking after payment
     */
    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<Booking> confirmBooking(@PathVariable Long bookingId) {
        try {
            Booking confirmedBooking = bookingService.confirmBooking(bookingId);
            return new ResponseEntity<>(confirmedBooking, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check booking validity
     */
    @GetMapping("/{bookingId}/validate")
    public ResponseEntity<ValidationResponse> validateBooking(@PathVariable Long bookingId) {
        try {
            boolean isValid = bookingService.validateBooking(bookingId);
            ValidationResponse response = new ValidationResponse(isValid);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get bookings by status
     */
    @PostMapping("/status")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@RequestBody StatusRequest request) {
        try {
            List<Booking> bookings = bookingService.getBookingsByStatus(request.getStatus());
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get user bookings by status
     */
    @PostMapping("/user/status")
    public ResponseEntity<List<Booking>> getUserBookingsByStatus(@RequestBody UserStatusRequest request) {
        try {
            List<Booking> bookings = bookingService.getUserBookingsByStatus(request.getUserId(), request.getStatus());
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get event bookings by status
     */
    @PostMapping("/event/status")
    public ResponseEntity<List<Booking>> getEventBookingsByStatus(@RequestBody EventStatusRequest request) {
        try {
            List<Booking> bookings = bookingService.getEventBookingsByStatus(request.getEventId(), request.getStatus());
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get total revenue for an event
     */
    @PostMapping("/event/revenue")
    public ResponseEntity<RevenueResponse> getEventRevenue(@RequestBody EventRequest request) {
        try {
            BigDecimal revenue = bookingService.getEventRevenue(request.getEventId());
            RevenueResponse response = new RevenueResponse(revenue);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Complete booking (mark as completed after event)
     */
    @PutMapping("/{bookingId}/complete")
    public ResponseEntity<Booking> completeBooking(@PathVariable Long bookingId) {
        try {
            Booking completedBooking = bookingService.completeBooking(bookingId);
            return new ResponseEntity<>(completedBooking, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Request DTO classes for @RequestBody parameters

    public static class BookingReferenceRequest {
        private String bookingReference;

        public String getBookingReference() { return bookingReference; }
        public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    }

    public static class UserRequest {
        private Long userId;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    public static class EventRequest {
        private Long eventId;

        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }
    }

    public static class StatusRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class UserStatusRequest {
        private Long userId;
        private String status;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class EventStatusRequest {
        private Long eventId;
        private String status;

        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class ValidationResponse {
        private boolean valid;

        public ValidationResponse(boolean valid) {
            this.valid = valid;
        }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
    }

    public static class RevenueResponse {
        private BigDecimal revenue;

        public RevenueResponse(BigDecimal revenue) {
            this.revenue = revenue;
        }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    }
}