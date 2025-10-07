package com.amdocs.spx.service;


import com.amdocs.spx.entity.Event;
import com.amdocs.spx.entity.TicketType;
import com.amdocs.spx.exception.ResourceNotFoundException;
import com.amdocs.spx.repository.EventRepository;
import com.amdocs.spx.repository.TicketTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TicketTypeService {

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private EventRepository eventRepository;

    /**
     * Add ticket type to event
     */
    public TicketType createTicketType(TicketType ticketType) {
        // Validate event exists
        if (ticketType.getEvent() != null && ticketType.getEvent().getEventId() != null) {
            Event event = eventRepository.findById(ticketType.getEvent().getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
            ticketType.setEvent(event);
        } else {
            throw new IllegalArgumentException("Event is required for ticket type");
        }

        // Set default values
        if (ticketType.getQuantitySold() == null) {
            ticketType.setQuantitySold(0);
        }
        if (ticketType.getIsActive() == null) {
            ticketType.setIsActive(true);
        }

        // Validate quantity
        if (ticketType.getQuantityAvailable() <= 0) {
            throw new IllegalArgumentException("Quantity available must be greater than 0");
        }

        // Validate price
        if (ticketType.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        return ticketTypeRepository.save(ticketType);
    }

    /**
     * Modify ticket type details
     */
    public TicketType updateTicketType(Long ticketTypeId, TicketType ticketTypeDetails) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + ticketTypeId));

        if (ticketTypeDetails.getTypeName() != null) {
            ticketType.setTypeName(ticketTypeDetails.getTypeName());
        }
        if (ticketTypeDetails.getPrice() != null) {
            if (ticketTypeDetails.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }
            ticketType.setPrice(ticketTypeDetails.getPrice());
        }
        if (ticketTypeDetails.getQuantityAvailable() != null) {
            // Ensure new quantity is not less than already sold tickets
            if (ticketTypeDetails.getQuantityAvailable() < ticketType.getQuantitySold()) {
                throw new IllegalArgumentException("Quantity available cannot be less than quantity already sold");
            }
            ticketType.setQuantityAvailable(ticketTypeDetails.getQuantityAvailable());
        }

        return ticketTypeRepository.save(ticketType);
    }

    /**
     * Remove ticket type
     */
    public void deleteTicketType(Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + ticketTypeId));

        // Check if any tickets have been sold
        if (ticketType.getQuantitySold() > 0) {
            throw new IllegalStateException("Cannot delete ticket type with sold tickets");
        }

        // Check if there are any bookings
        if (ticketType.getBookings() != null && !ticketType.getBookings().isEmpty()) {
            throw new IllegalStateException("Cannot delete ticket type with existing bookings");
        }

        ticketTypeRepository.delete(ticketType);
    }

    /**
     * Get ticket type details
     */
    public TicketType getTicketTypeById(Long ticketTypeId) {
        return ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + ticketTypeId));
    }

    /**
     * Get all ticket types for an event
     */
    public List<TicketType> getTicketTypesByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        return ticketTypeRepository.findByEvent(event);
    }

    /**
     * Update available quantity
     */
    public TicketType updateTicketAvailability(Long ticketTypeId, Integer newQuantity) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + ticketTypeId));

        // Ensure new quantity is not less than already sold tickets
        if (newQuantity < ticketType.getQuantitySold()) {
            throw new IllegalArgumentException("New quantity cannot be less than quantity already sold (" +
                    ticketType.getQuantitySold() + ")");
        }

        ticketType.setQuantityAvailable(newQuantity);
        return ticketTypeRepository.save(ticketType);
    }

    /**
     * Check if tickets available
     */
    public boolean checkTicketAvailability(Long ticketTypeId, Integer requestedQuantity) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + ticketTypeId));

        if (!ticketType.getIsActive()) {
            return false;
        }

        int availableTickets = ticketType.getQuantityAvailable() - ticketType.getQuantitySold();
        return availableTickets >= requestedQuantity;
    }

    /**
     * Get remaining tickets count
     */
    public Integer getRemainingTickets(Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + ticketTypeId));

        return ticketType.getQuantityAvailable() - ticketType.getQuantitySold();
    }

    /**
     * Enable ticket type
     */
    public TicketType activateTicketType(Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + ticketTypeId));

        ticketType.setIsActive(true);
        return ticketTypeRepository.save(ticketType);
    }

    /**
     * Disable ticket type
     */
    public TicketType deactivateTicketType(Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + ticketTypeId));

        ticketType.setIsActive(false);
        return ticketTypeRepository.save(ticketType);
    }

    /**
     * Get active ticket types for an event
     */
    public List<TicketType> getActiveTicketTypesByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        return ticketTypeRepository.findByEventAndIsActive(event, true);
    }

    /**
     * Increment sold tickets (used during booking)
     */
    public TicketType incrementSoldTickets(Long ticketTypeId, Integer quantity) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + ticketTypeId));

        // Check availability
        if (!checkTicketAvailability(ticketTypeId, quantity)) {
            throw new IllegalStateException("Not enough tickets available");
        }

        ticketType.setQuantitySold(ticketType.getQuantitySold() + quantity);
        return ticketTypeRepository.save(ticketType);
    }

    /**
     * Decrement sold tickets (used during booking cancellation)
     */
    public TicketType decrementSoldTickets(Long ticketTypeId, Integer quantity) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + ticketTypeId));

        int newQuantitySold = ticketType.getQuantitySold() - quantity;
        if (newQuantitySold < 0) {
            throw new IllegalStateException("Cannot decrement tickets below zero");
        }

        ticketType.setQuantitySold(newQuantitySold);
        return ticketTypeRepository.save(ticketType);
    }
}
