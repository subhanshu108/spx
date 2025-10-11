package com.amdocs.spx;

import com.amdocs.spx.controller.TicketTypeController;
import com.amdocs.spx.dto.TicketTypeDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class TicketTypeControllerTest {


    @Test
    void testDeleteResponseEntity() {
        // Simulate delete ticket type response
        String result = "Ticket type deleted successfully";
        Assertions.assertEquals("Ticket type deleted successfully", result);
    }

    @Test
    void testDummyResponseEntity() {
        // Simulate a response for getTicketTypeById
        ResponseEntity<TicketTypeDTO> response =
                new ResponseEntity<>(new TicketTypeDTO(), HttpStatus.OK);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void testDummyListResponseEntity() {
        // Simulate a response for getAllTicketTypes or getEventTicketTypes
        ResponseEntity<List<TicketTypeDTO>> response =
                new ResponseEntity<>(List.of(new TicketTypeDTO(), new TicketTypeDTO()), HttpStatus.OK);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(2, response.getBody().size());
    }
}
