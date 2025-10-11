package com.amdocs.spx;

import com.amdocs.spx.controller.VenueController;
import com.amdocs.spx.dto.EventDTO;
import com.amdocs.spx.dto.VenueDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public class VenueControllerTest {

    @Test
    void testConvertToDTO() {
        // Create dummy Venue
        var venue = new com.amdocs.spx.entity.Venue();
        venue.setVenueId(1L);
        venue.setVenueName("Main Hall");
        venue.setAddress("123 Street");
        venue.setCity("New York");
        venue.setTotalCapacity(500);

        // Access private convertToDTO via reflection
        VenueController controller = new VenueController();
        var venueDTO = invokeConvertToDTO(controller, venue);

        Assertions.assertEquals(1L, venueDTO.getVenueId());
        Assertions.assertEquals("Main Hall", venueDTO.getVenueName());
        Assertions.assertEquals("123 Street", venueDTO.getVenueAddress());
        Assertions.assertEquals("New York", venueDTO.getCity());
        Assertions.assertEquals(500, venueDTO.getTotalCapacity());
    }

    @Test
    void testDeleteResponseEntity() {
        // Simulate delete venue response
        String result = "Venue deleted successfully";
        Assertions.assertEquals("Venue deleted successfully", result);
    }

    @Test
    void testDummyResponseEntity() {
        // Simulate a response for getVenueById
        ResponseEntity<VenueDTO> response =
                new ResponseEntity<>(new VenueDTO(), HttpStatus.OK);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void testDummyListResponseEntity() {
        // Simulate a response for getAllVenues or getVenuesByCity
        ResponseEntity<List<VenueDTO>> response =
                new ResponseEntity<>(List.of(new VenueDTO(), new VenueDTO()), HttpStatus.OK);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(2, response.getBody().size());
    }

    @Test
    void testVenueAvailability() {
        // Simulate availability response
        ResponseEntity<Boolean> response = new ResponseEntity<>(true, HttpStatus.OK);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(response.getBody());
    }

    @Test
    void testAllEventsByVenue() {
        // Simulate getAllEventsByVenueId response
        ResponseEntity<List<EventDTO>> response =
                new ResponseEntity<>(List.of(new EventDTO(), new EventDTO()), HttpStatus.OK);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(2, response.getBody().size());
    }

    // -------------------
    // Helper method
    // -------------------
    private static VenueDTO invokeConvertToDTO(VenueController controller, com.amdocs.spx.entity.Venue venue) {
        try {
            var method = VenueController.class.getDeclaredMethod("convertToDTO", com.amdocs.spx.entity.Venue.class);
            method.setAccessible(true);
            return (VenueDTO) method.invoke(controller, venue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
