package com.amdocs.spx.controller;

import com.amdocs.spx.entity.Event;
import com.amdocs.spx.entity.Venue;
import com.amdocs.spx.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/venues")
public class VenueController {

    @Autowired
    private VenueService venueService;

    // Create a new venue - Use @RequestBody for JSON payload
    @PostMapping
    public ResponseEntity<Venue> createVenue(@RequestBody Venue venue) {
        try {
            Venue createdVenue = venueService.createVenue(venue);
            return new ResponseEntity<>(createdVenue, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update an existing venue - Use @RequestBody for JSON payload
    @PutMapping("/{venueId}")
    public ResponseEntity<Venue> updateVenue(@PathVariable Long venueId, @RequestBody Venue venue) {
        try {
            // Ensure the venue ID in path matches the venue object ID
            if (!venueId.equals(venue.getVenueId())) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            Venue updatedVenue = venueService.updateVenue(venue);
            return new ResponseEntity<>(updatedVenue, HttpStatus.OK);
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
    public ResponseEntity<List<Venue>> getAllVenues() {
        try {
            List<Venue> venues = venueService.getAllVenue();
            return new ResponseEntity<>(venues, HttpStatus.OK);
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

    // Get all events for a specific venue - Use @PathVariable for ID
    @GetMapping("/{venueId}/events")
    public ResponseEntity<List<Event>> getAllEventsByVenueId(@PathVariable Long venueId) {
        try {
            List<Event> events = venueService.getAllEventsByVenueId(venueId);
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}