package com.amdocs.spx.controller;

import com.amdocs.spx.dto.EventDTO;
import com.amdocs.spx.entity.Event;
import com.amdocs.spx.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * Create new event
     */
    @PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<EventDTO> createEvent(@RequestBody EventDTO eventDTO) {
        try {
            Event event = convertToEntity(eventDTO);
            Event createdEvent = eventService.createEvent(event);
            EventDTO responseDTO = convertToDTO(createdEvent);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Modify event details
     */
    @PutMapping("/{eventId}")
    public ResponseEntity<EventDTO> updateEvent(@PathVariable Long eventId, @RequestBody EventDTO eventDTO) {
        try {
            Event eventDetails = convertToEntity(eventDTO);
            Event updatedEvent = eventService.updateEvent(eventId, eventDetails);
            System.out.println(updatedEvent.getStatus());
            EventDTO responseDTO = convertToDTO(updatedEvent);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cancel/delete event
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long eventId) {
        try {
            eventService.deleteEvent(eventId);
            return new ResponseEntity<>("Event deleted successfully", HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting event", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get event details
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable Long eventId) {
        try {
            Event event = eventService.getEventById(eventId);
            EventDTO eventDTO = convertToDTO(event);
            return new ResponseEntity<>(eventDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * List all events
     */
    @GetMapping("/getAllEvents")
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        try {
            List<Event> events = eventService.getAllEvents();
            List<EventDTO> eventDTOs = events.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(eventDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Filter events by category
     */
    @PostMapping("/category")
    public ResponseEntity<List<EventDTO>> getEventsByCategory(@RequestBody CategoryRequest request) {
        try {
            List<Event> events = eventService.getEventsByCategory(request.getCategory());
            List<EventDTO> eventDTOs = events.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(eventDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get events by organizer
     */
    @PostMapping("/organizer")
    public ResponseEntity<List<EventDTO>> getEventsByOrganizer(@RequestBody OrganizerRequest request) {
        try {
            List<Event> events = eventService.getEventsByOrganizer(request.getOrganizerId());
            List<EventDTO> eventDTOs = events.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(eventDTOs, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get future events
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<EventDTO>> getUpcomingEvents() {
        try {
            List<Event> events = eventService.getUpcomingEvents();
            List<EventDTO> eventDTOs = events.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(eventDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search events by criteria
     */
    @PostMapping("/search")
    public ResponseEntity<List<EventDTO>> searchEvents(@RequestBody SearchRequest request) {
        try {
            List<Event> events = eventService.searchEvents(request.getKeyword());
            List<EventDTO> eventDTOs = events.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(eventDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Change event status
     */
    @PutMapping("/{eventId}/status")
    public ResponseEntity<EventDTO> updateEventStatus(@PathVariable Long eventId, @RequestBody StatusRequest request) {
        try {
            Event updatedEvent = eventService.updateEventStatus(eventId, request.getStatus());
            EventDTO eventDTO = convertToDTO(updatedEvent);
            return new ResponseEntity<>(eventDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get events at specific venue
     */
    @PostMapping("/venue")
    public ResponseEntity<List<EventDTO>> getEventsByVenue(@RequestBody VenueRequest request) {
        try {
            List<Event> events = eventService.getEventsByVenue(request.getVenueId());
            List<EventDTO> eventDTOs = events.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(eventDTOs, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Conversion methods

    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setEventId(event.getEventId());
        dto.setEventName(event.getEventName());

        dto.setCategory(event.getCategory());
        dto.setEventDate(event.getEventDate());
        dto.setStatus(event.getStatus());
        dto.setBannerImageUrl(event.getBannerImageUrl());
        dto.setTotalTicketsAvailable(event.getTotalTicketsAvailable());
        dto.setTicketsSold(event.getTicketsSold());

        // Convert Venue
        if (event.getVenue() != null) {
            EventDTO.VenueDTO venueDTO = new EventDTO.VenueDTO();
            venueDTO.setVenueId(event.getVenue().getVenueId());
            venueDTO.setVenueName(event.getVenue().getVenueName());
            venueDTO.setAddress(event.getVenue().getAddress());
            venueDTO.setCity(event.getVenue().getCity());
            venueDTO.setCapacity(event.getVenue().getTotalCapacity());
            dto.setVenue(venueDTO);
        }

        // Convert Organizer
        if (event.getOrganizer() != null) {
            EventDTO.OrganizerDTO organizerDTO = new EventDTO.OrganizerDTO();
            organizerDTO.setUserId(event.getOrganizer().getUserId());
            organizerDTO.setFirstName(event.getOrganizer().getFirstName());
            organizerDTO.setLastName(event.getOrganizer().getLastName());
            organizerDTO.setEmail(event.getOrganizer().getEmail());
            dto.setOrganizer(organizerDTO);
        }

        // Convert Ticket Types
        if (event.getTicketTypes() != null && !event.getTicketTypes().isEmpty()) {
            List<EventDTO.TicketTypeDTO> ticketTypeDTOs = event.getTicketTypes().stream()
                    .map(tt -> {
                        EventDTO.TicketTypeDTO ttDTO = new EventDTO.TicketTypeDTO();
                        ttDTO.setTicketTypeId(tt.getTicketTypeId());
                        ttDTO.setTypeName(tt.getTypeName());

                        ttDTO.setPrice(tt.getPrice().doubleValue());
                        ttDTO.setQuantityAvailable(tt.getQuantityAvailable());
                        ttDTO.setQuantitySold(tt.getQuantitySold());
                        ttDTO.setIsActive(tt.getIsActive());
                        return ttDTO;
                    })
                    .collect(Collectors.toList());
            dto.setTicketTypes(ticketTypeDTOs);
        }

        return dto;
    }

    private Event convertToEntity(EventDTO dto) {
        Event event = new Event();
        event.setEventId(dto.getEventId());
        event.setEventName(dto.getEventName());

        event.setCategory(dto.getCategory());
        event.setEventDate(dto.getEventDate());
        event.setStatus(dto.getStatus());
        event.setBannerImageUrl(dto.getBannerImageUrl());
        event.setTotalTicketsAvailable(dto.getTotalTicketsAvailable());
        event.setTicketsSold(dto.getTicketsSold());

        // Note: For venue and organizer, you'll need to set them with just IDs
        // The service layer should handle fetching the full entities
        if (dto.getVenue() != null && dto.getVenue().getVenueId() != null) {
            com.amdocs.spx.entity.Venue venue = new com.amdocs.spx.entity.Venue();
            venue.setVenueId(dto.getVenue().getVenueId());
            event.setVenue(venue);
        }

        if (dto.getOrganizer() != null && dto.getOrganizer().getUserId() != null) {
            com.amdocs.spx.entity.User organizer = new com.amdocs.spx.entity.User();
            organizer.setUserId(dto.getOrganizer().getUserId());
            event.setOrganizer(organizer);
        }

        return event;
    }

    @DeleteMapping("/deleteEvent/{id}")
    public void deleteEventById(@PathVariable("id") Long id) {
         eventService.deleteEvent(id);
         System.out.println("Delete event with id: " + id);
    }


    // Request DTO classes for @RequestBody parameters

    public static class CategoryRequest {
        private String category;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    public static class OrganizerRequest {
        private Long organizerId;

        public Long getOrganizerId() { return organizerId; }
        public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }
    }

    public static class SearchRequest {
        private String keyword;

        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
    }

    public static class StatusRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class VenueRequest {
        private Long venueId;

        public Long getVenueId() { return venueId; }
        public void setVenueId(Long venueId) { this.venueId = venueId; }
    }
}