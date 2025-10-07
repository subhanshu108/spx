package com.amdocs.spx.service;


import com.amdocs.spx.entity.Booking;
import com.amdocs.spx.entity.Event;
import com.amdocs.spx.entity.TicketType;
import com.amdocs.spx.entity.User;
import com.amdocs.spx.exception.ResourceNotFoundException;
import com.amdocs.spx.repository.BookingRepository;
import com.amdocs.spx.repository.EventRepository;
import com.amdocs.spx.repository.TicketTypeRepository;
import com.amdocs.spx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private TicketTypeService ticketTypeService;

    /**
     * Create new booking
     */
    public Booking createBooking(Booking booking) {
        // Validate user exists
        if (booking.getUser() != null && booking.getUser().getUserId() != null) {
            User user = userRepository.findById(booking.getUser().getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            booking.setUser(user);
        } else {
            throw new IllegalArgumentException("User is required for booking");
        }

        // Validate event exists
        if (booking.getEvent() != null && booking.getEvent().getEventId() != null) {
            Event event = eventRepository.findById(booking.getEvent().getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
            booking.setEvent(event);
        } else {
            throw new IllegalArgumentException("Event is required for booking");
        }

        // Validate ticket type exists
        if (booking.getTicketType() != null && booking.getTicketType().getTicketTypeId() != null) {
            TicketType ticketType = ticketTypeRepository.findById(booking.getTicketType().getTicketTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));
            booking.setTicketType(ticketType);

            // Check ticket availability
            if (!ticketTypeService.checkTicketAvailability(ticketType.getTicketTypeId(), booking.getQuantity())) {
                throw new IllegalStateException("Not enough tickets available");
            }

            // Calculate total amount
            BigDecimal totalAmount = ticketType.getPrice().multiply(new BigDecimal(booking.getQuantity()));
            booking.setTotalAmount(totalAmount);
        } else {
            throw new IllegalArgumentException("Ticket type is required for booking");
        }

        // Validate quantity
        if (booking.getQuantity() == null || booking.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        // Generate unique booking reference
        booking.setBookingReference(generateBookingReference());

        // Set default values
        if (booking.getBookingStatus() == null) {
            booking.setBookingStatus("PENDING");
        }
        if (booking.getBookingDate() == null) {
            booking.setBookingDate(LocalDateTime.now());
        }

        // Reserve tickets (increment sold count)
        ticketTypeService.incrementSoldTickets(booking.getTicketType().getTicketTypeId(), booking.getQuantity());

        return bookingRepository.save(booking);
    }

    /**
     * Get booking details
     */
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
    }

    /**
     * Find booking by reference number
     */
    public Booking getBookingByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + bookingReference));
    }

    /**
     * Get all bookings for a user
     */
    public List<Booking> getUserBookings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return bookingRepository.findByUser(user);
    }

    /**
     * Get all bookings for an event
     */
    public List<Booking> getEventBookings(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        return bookingRepository.findByEvent(event);
    }

    /**
     * Change booking status
     */
    public Booking updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // Validate status
        List<String> validStatuses = Arrays.asList("PENDING", "CONFIRMED", "CANCELLED", "COMPLETED");
        if (!validStatuses.contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Invalid status. Valid statuses are: " + validStatuses);
        }

        booking.setBookingStatus(status.toUpperCase());
        return bookingRepository.save(booking);
    }

    /**
     * Cancel a booking
     */
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // Check if booking can be cancelled
        if ("CANCELLED".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if ("COMPLETED".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Cannot cancel a completed booking");
        }

        // Release tickets (decrement sold count)
        ticketTypeService.decrementSoldTickets(booking.getTicketType().getTicketTypeId(), booking.getQuantity());

        // Update status to cancelled
        booking.setBookingStatus("CANCELLED");
        return bookingRepository.save(booking);
    }

    /**
     * Confirm booking after payment
     */
    public Booking confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // Check if booking is in pending status
        if (!"PENDING".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Only pending bookings can be confirmed");
        }

        // Validate booking is still valid
        if (!validateBooking(bookingId)) {
            throw new IllegalStateException("Booking is no longer valid");
        }

        booking.setBookingStatus("CONFIRMED");
        return bookingRepository.save(booking);
    }

    /**
     * Check booking validity
     */
    public boolean validateBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // Check if event is still upcoming
        Event event = booking.getEvent();
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Check if event is not cancelled
        if ("CANCELLED".equals(event.getStatus())) {
            return false;
        }

        // Check if ticket type is still active
        if (!booking.getTicketType().getIsActive()) {
            return false;
        }

        // Check if booking is not cancelled
        if ("CANCELLED".equals(booking.getBookingStatus())) {
            return false;
        }

        return true;
    }

    /**
     * Get bookings by status
     */
    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByBookingStatus(status.toUpperCase());
    }

    /**
     * Get user bookings by status
     */
    public List<Booking> getUserBookingsByStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return bookingRepository.findByUserAndBookingStatus(user, status.toUpperCase());
    }

    /**
     * Get event bookings by status
     */
    public List<Booking> getEventBookingsByStatus(Long eventId, String status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        return bookingRepository.findByEventAndBookingStatus(event, status.toUpperCase());
    }

    /**
     * Get total revenue for an event
     */
    public BigDecimal getEventRevenue(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        List<Booking> confirmedBookings = bookingRepository.findByEventAndBookingStatus(event, "CONFIRMED");

        return confirmedBookings.stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Generate unique booking reference
     */
    private String generateBookingReference() {
        String reference;
        do {
            // Generate format: BKG-YYYYMMDD-XXXXXX (random 6 digit number)
            String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String randomPart = String.format("%06d", (int) (Math.random() * 1000000));
            reference = "BKG-" + datePart + "-" + randomPart;
        } while (bookingRepository.findByBookingReference(reference).isPresent());

        return reference;
    }

    /**
     * Complete booking (mark as completed after event)
     */
    public Booking completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!"CONFIRMED".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Only confirmed bookings can be completed");
        }

        booking.setBookingStatus("COMPLETED");
        return bookingRepository.save(booking);
    }
}
