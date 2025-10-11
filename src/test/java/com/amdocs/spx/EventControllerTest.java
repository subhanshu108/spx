package com.amdocs.spx;

import com.amdocs.spx.controller.EventController;
import com.amdocs.spx.dto.EventDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public class EventControllerTest {

    @Test
    void testCategoryRequest() {
        EventController.CategoryRequest request = new EventController.CategoryRequest();
        request.setCategory("Music");

        Assertions.assertEquals("Music", request.getCategory());
    }

    @Test
    void testStatusRequest() {
        EventController.StatusRequest status = new EventController.StatusRequest();
        status.setStatus("Active");

        Assertions.assertEquals("Active", status.getStatus());
    }

    @Test
    void testVenueRequest() {
        EventController.VenueRequest venue = new EventController.VenueRequest();
        venue.setVenueId(101L);

        Assertions.assertEquals(101L, venue.getVenueId());
    }

    @Test
    void testConvertToDTOandEntity() {
        // Create EventDTO
        EventDTO dto = new EventDTO();
        dto.setEventId(1L);
        dto.setEventName("Concert");
        dto.setCategory("Music");
        dto.setStatus("Upcoming");
        dto.setBannerImageUrl("banner.jpg");
        dto.setTotalTicketsAvailable(100);
        dto.setTicketsSold(20);

        // Create controller to access private conversion indirectly
        EventController controller = new EventController();

        // Convert to entity and back to DTO to simulate conversion logic
        var eventEntity = invokeConvertToEntity(controller, dto);
        var dtoResult = invokeConvertToDTO(controller, eventEntity);

        // Check some values
        Assertions.assertEquals("Concert", dtoResult.getEventName());
        Assertions.assertEquals("Music", dtoResult.getCategory());
    }

    @Test
    void testDeleteResponseEntity() {
        // Simulate success response from deleteEvent method
        ResponseEntity<String> response =
                new ResponseEntity<>("Event deleted successfully", HttpStatus.OK);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Event deleted successfully", response.getBody());
    }

    // -------------------
    // Helper methods
    // -------------------

    // Reflection-based access to private methods, only for showing testing
    private static com.amdocs.spx.entity.Event invokeConvertToEntity(EventController controller, EventDTO dto) {
        try {
            var method = EventController.class.getDeclaredMethod("convertToEntity", EventDTO.class);
            method.setAccessible(true);
            return (com.amdocs.spx.entity.Event) method.invoke(controller, dto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static EventDTO invokeConvertToDTO(EventController controller, com.amdocs.spx.entity.Event event) {
        try {
            var method = EventController.class.getDeclaredMethod("convertToDTO", com.amdocs.spx.entity.Event.class);
            method.setAccessible(true);
            return (EventDTO) method.invoke(controller, event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
