package com.amdocs.spx.service;


import com.amdocs.spx.entity.Event;
import com.amdocs.spx.entity.User;
import com.amdocs.spx.entity.Venue;
import com.amdocs.spx.repository.EventRepository;
import com.amdocs.spx.repository.UserRepository;
import com.amdocs.spx.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     * Create new event
     */
    public Event createEvent(Event event) {
        // Validate venue exists
        if (event.getVenue() != null && event.getVenue().getVenueId() != null) {
            Venue venue = venueRepository.findById(event.getVenue().getVenueId())
                    .orElseThrow(() -> new RuntimeException("Venue not found"));
            event.setVenue(venue);
        }

        // Validate organizer exists
        if (event.getOrganizer() != null && event.getOrganizer().getUserId() != null) {
            User organizer = userRepository.findById(event.getOrganizer().getUserId())
                    .orElseThrow(() -> new RuntimeException("Organizer not found"));
            event.setOrganizer(organizer);
        }

        // Set default values
        if (event.getStatus() == null) {
            event.setStatus("UPCOMING");
        }
        if (event.getTicketsSold() == null) {
            event.setTicketsSold(0);
        }

        return eventRepository.save(event);
    }

    /**
     * Modify event details
     */
    public Event updateEvent(Long eventId, Event eventDetails) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        if (eventDetails.getEventName() != null) {
            event.setEventName(eventDetails.getEventName());
        }
        if (eventDetails.getDescription() != null) {
            event.setDescription(eventDetails.getDescription());
        }
        if (eventDetails.getCategory() != null) {
            event.setCategory(eventDetails.getCategory());
        }
        if (eventDetails.getEventDate() != null) {
            event.setEventDate(eventDetails.getEventDate());
        }
        if (eventDetails.getBannerImageUrl() != null) {
            event.setBannerImageUrl(eventDetails.getBannerImageUrl());
        }
        if (eventDetails.getTotalTicketsAvailable() != null) {
            event.setTotalTicketsAvailable(eventDetails.getTotalTicketsAvailable());
        }
        if (eventDetails.getVenue() != null && eventDetails.getVenue().getVenueId() != null) {
            Venue venue = venueRepository.findById(eventDetails.getVenue().getVenueId())
                    .orElseThrow(() -> new RuntimeException("Venue not found"));
            event.setVenue(venue);
        }

        return eventRepository.save(event);
    }

    /**
     * Cancel/delete event
     */
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Check if event has bookings
        if (event.getBookings() != null && !event.getBookings().isEmpty()) {
            throw new IllegalStateException("Cannot delete event with existing bookings");
        }

        eventRepository.delete(event);
    }

    /**
     * Get event details
     */
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
    }

    /**
     * List all events
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Filter events by category
     */
    public List<Event> getEventsByCategory(String category) {
        return eventRepository.findByCategory(category);
    }

    /**
     * Get events by organizer
     */
    public List<Event> getEventsByOrganizer(Long organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new RuntimeException("Organizer not found with id: " + organizerId));
        return eventRepository.findByOrganizer(organizer);
    }

    /**
     * Get future events
     */
    public List<Event> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findByEventDateAfterAndStatus(now, "UPCOMING");
    }

    /**
     * Search events by criteria (name or description)
     */
    public List<Event> searchEvents(String keyword) {
        return eventRepository.findByEventNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }

    /**
     * Change event status
     */
    public Event updateEventStatus(Long eventId, String status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Validate status
        List<String> validStatuses = Arrays.asList("UPCOMING", "ONGOING", "COMPLETED", "CANCELLED");
        if (!validStatuses.contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Invalid status. Valid statuses are: " + validStatuses);
        }

        event.setStatus(status.toUpperCase());
        return eventRepository.save(event);
    }

    /**
     * Get events at specific venue
     */
    public List<Event> getEventsByVenue(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found with id: " + venueId));
        return eventRepository.findByVenue(venue);
    }
}