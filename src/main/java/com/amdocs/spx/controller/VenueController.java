package com.amdocs.spx.controller;

import com.amdocs.spx.dto.EventDTO;
import com.amdocs.spx.dto.VenueDTO;
import com.amdocs.spx.entity.Event;
import com.amdocs.spx.entity.Venue;
import com.amdocs.spx.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/venues")
@CrossOrigin(origins = "*")
public class VenueController {

    @Autowired
    private VenueService venueService;

    private VenueDTO convertToDTO(Venue venue) {
        VenueDTO venueDTO = new VenueDTO();
        venueDTO.setVenueId(venue.getVenueId());
        venueDTO.setVenueName(venue.getVenueName());
        venueDTO.setVenueAddress(venue.getAddress());
        venueDTO.setCity(venue.getCity());
        venueDTO.setTotalCapacity(venue.getTotalCapacity());
        return venueDTO;
    }

    // Create a new venue - Use @RequestBody for JSON payload
    @PostMapping("/createVenue")
    public ResponseEntity<VenueDTO> createVenue(@RequestBody Venue venue) {
        try {
            Venue createdVenue = venueService.createVenue(venue);
            VenueDTO venueDTO = convertToDTO(createdVenue);
            return new ResponseEntity<>(venueDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update an existing venue - Use @RequestBody for JSON payload
    @PutMapping("/{venueId}")
    public ResponseEntity<VenueDTO> updateVenue(@PathVariable Long venueId, @RequestBody Venue venue) {
        try {
            // Ensure the venue ID in path matches the venue object ID
            if (!venueId.equals(venue.getVenueId())) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            Venue updatedVenue = venueService.updateVenue(venue);
            VenueDTO venueDTO = convertToDTO(updatedVenue);
            return new ResponseEntity<>(venueDTO, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a venue - Use @PathVariable for ID
    @DeleteMapping("/{venueId}")
    public ResponseEntity<String> deleteVenue(@PathVariable Long venueId) {
        try {
            Optional<Venue> venue = venueService.getVenueById(venueId);
            if (venue.isPresent()) {
                String result = venueService.deleteVenue(venue.get());
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Venue not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting venue", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get venue by ID - Use @PathVariable for ID
    @GetMapping("/{venueId}")
    public ResponseEntity<Venue> getVenueById(@PathVariable Long venueId) {
        try {
            Optional<Venue> venue = venueService.getVenueById(venueId);
            if (venue.isPresent()) {
                return new ResponseEntity<>(venue.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all venues - No parameters needed
    @GetMapping("/getVenues")
    public ResponseEntity<List<VenueDTO>> getAllVenues() {
        try {
            List<Venue> venues = venueService.getAllVenue();
            List<VenueDTO> venueDTOList = new ArrayList<>();

            for(Venue venue : venues){
                venueDTOList.add(convertToDTO(venue));
            }

            return new ResponseEntity<>(venueDTOList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get venues by city - Use @PathVariable for city
    @GetMapping("/city/{city}")
    public ResponseEntity<List<Venue>> getVenuesByCity(@PathVariable String city) {
        try {
            List<Venue> allVenues = venueService.getAllVenue();
            List<Venue> venuesByCity = allVenues.stream()
                    .filter(venue -> city.equalsIgnoreCase(venue.getCity()))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(venuesByCity, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Check venue availability - Use @RequestParam for optional parameters
    @GetMapping("/{venueId}/availability")
    public ResponseEntity<Boolean> checkVenueAvailability(
            @PathVariable Long venueId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Optional<Venue> venue = venueService.getVenueById(venueId);
            if (venue.isPresent()) {
                List<Event> events = venueService.getAllEventsByVenueId(venueId);

                boolean isAvailable = events.stream()
                        .noneMatch(event -> event.getEventDate().equals(date));

                return new ResponseEntity<>(isAvailable, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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
    // Get all events for a specific venue - Use @PathVariable for ID
    @GetMapping("/{venueId}/events")
    public ResponseEntity<List<EventDTO>> getAllEventsByVenueId(@PathVariable Long venueId) {
        try {
            List<Event> events = venueService.getAllEventsByVenueId(venueId);
            List<EventDTO> dtoList = new ArrayList<>();
            for(Event event : events) {
                dtoList.add(convertToDTO(event));
            }
            return new ResponseEntity<>(dtoList, HttpStatus.OK);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}