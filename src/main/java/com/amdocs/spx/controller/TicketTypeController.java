package com.amdocs.spx.controller;

import com.amdocs.spx.dto.TicketTypeDTO;
import com.amdocs.spx.entity.Event;
import com.amdocs.spx.entity.TicketType;
import com.amdocs.spx.repository.TicketTypeRepository;
import com.amdocs.spx.service.TicketTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/ticket-types")
public class TicketTypeController {

    @Autowired
    private TicketTypeService ticketTypeService;
    @Autowired
    private TicketTypeRepository ticketTypeRepository;
    /**
     * Add ticket type to event
     */
    @PostMapping("/create-ticket")
    public TicketTypeDTO createTicketType(@RequestBody TicketType ticketType) {

//        private Long eventId;
//        private String typeName;
//        private BigDecimal price;
//        private Integer quantityAvailable;
//        private Integer quantitySold;
//        private Boolean isActive;
        TicketTypeDTO ticketTypeDTO = new TicketTypeDTO();
        return getTicketTypeDTO(ticketTypeDTO, ticketType);
    }

    /**
     * Modify ticket type details
     */
    @PutMapping("/{ticketTypeId}")
    public TicketTypeDTO updateTicketType(@PathVariable Long ticketTypeId, @RequestBody TicketType ticketTypeDetails) {
        TicketTypeDTO ticketTypeDTO = new TicketTypeDTO();
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return getTicketTypeDTO(ticketTypeDTO, ticketType);
    }

    private TicketTypeDTO getTicketTypeDTO(TicketTypeDTO ticketTypeDTO, TicketType ticketType) {
        ticketTypeDTO.setEventId(ticketType.getEvent().getEventId());
        ticketTypeDTO.setTypeName(ticketType.getTypeName());
        ticketTypeDTO.setPrice(ticketType.getPrice());
        ticketTypeDTO.setQuantityAvailable(ticketType.getQuantityAvailable());
        ticketTypeDTO.setQuantitySold(ticketType.getQuantitySold());
        ticketTypeDTO.setIsActive(ticketType.getIsActive());
        return  ticketTypeDTO;
    }

    /**
     * Remove ticket type
     */
    @DeleteMapping("/{ticketTypeId}")
    public ResponseEntity<String> deleteTicketType(@PathVariable Long ticketTypeId) {
        try {
            ticketTypeService.deleteTicketType(ticketTypeId);
            return new ResponseEntity<>("Ticket type deleted successfully", HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting ticket type", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get ticket type details
     */
    @GetMapping("/{ticketTypeId}")
    public ResponseEntity<TicketTypeDTO> getTicketTypeById(@PathVariable Long ticketTypeId) {
        try {
            TicketType ticketType = ticketTypeService.getTicketTypeById(ticketTypeId);

            // Convert tickettype to DTO
            TicketTypeDTO ticketTypeDTO = getTicketTypeDTO(new TicketTypeDTO(), ticketType);

            return new ResponseEntity<>(ticketTypeDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all ticket types for an event
     */
    @PostMapping("/event")
    public ResponseEntity<List<TicketTypeDTO>> getTicketTypesByEvent(@RequestBody EventRequest request) {
        try {
            List<TicketType> ticketTypes = ticketTypeService.getTicketTypesByEvent(request.getEventId());

            // Convert each TicketType entity to TicketTypeDTO
            List<TicketTypeDTO> ticketTypeDTOs = ticketTypes.stream()
                    .map(ticketType -> getTicketTypeDTO(new TicketTypeDTO(), ticketType))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(ticketTypeDTOs, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Update available quantity
     */
    @PutMapping("/{ticketTypeId}/availability")
    public ResponseEntity<TicketType> updateTicketAvailability(@PathVariable Long ticketTypeId, @RequestBody QuantityRequest request) {
        try {
            TicketType updatedTicketType = ticketTypeService.updateTicketAvailability(ticketTypeId, request.getQuantity());
            return new ResponseEntity<>(updatedTicketType, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if tickets available
     */
    @PostMapping("/{ticketTypeId}/check-availability")
    public ResponseEntity<AvailabilityResponse> checkTicketAvailability(@PathVariable Long ticketTypeId, @RequestBody QuantityRequest request) {
        try {
            boolean isAvailable = ticketTypeService.checkTicketAvailability(ticketTypeId, request.getQuantity());
            AvailabilityResponse response = new AvailabilityResponse(isAvailable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get remaining tickets count
     */
    @GetMapping("/{ticketTypeId}/remaining")
    public ResponseEntity<RemainingTicketsResponse> getRemainingTickets(@PathVariable Long ticketTypeId) {
        try {
            Integer remainingTickets = ticketTypeService.getRemainingTickets(ticketTypeId);
            RemainingTicketsResponse response = new RemainingTicketsResponse(remainingTickets);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Enable ticket type
     */
    @PutMapping("/{ticketTypeId}/activate")
    public ResponseEntity<TicketType> activateTicketType(@PathVariable Long ticketTypeId) {
        try {
            TicketType activatedTicketType = ticketTypeService.activateTicketType(ticketTypeId);
            return new ResponseEntity<>(activatedTicketType, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Disable ticket type
     */
    @PutMapping("/{ticketTypeId}/deactivate")
    public ResponseEntity<TicketType> deactivateTicketType(@PathVariable Long ticketTypeId) {
        try {
            TicketType deactivatedTicketType = ticketTypeService.deactivateTicketType(ticketTypeId);
            return new ResponseEntity<>(deactivatedTicketType, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get active ticket types for an event
     */
    @PostMapping("/event/active")
    public ResponseEntity<List<TicketType>> getActiveTicketTypesByEvent(@RequestBody EventRequest request) {
        try {
            List<TicketType> activeTicketTypes = ticketTypeService.getActiveTicketTypesByEvent(request.getEventId());
            return new ResponseEntity<>(activeTicketTypes, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Request DTO classes for @RequestBody parameters

    public static class EventRequest {
        private Long eventId;

        public Long getEventId() { return eventId; }
        public void setEventId(Long eventId) { this.eventId = eventId; }
    }

    public static class QuantityRequest {
        private Integer quantity;

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class AvailabilityResponse {
        private boolean available;

        public AvailabilityResponse(boolean available) {
            this.available = available;
        }

        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }

    public static class RemainingTicketsResponse {
        private Integer remainingTickets;

        public RemainingTicketsResponse(Integer remainingTickets) {
            this.remainingTickets = remainingTickets;
        }

        public Integer getRemainingTickets() { return remainingTickets; }
        public void setRemainingTickets(Integer remainingTickets) { this.remainingTickets = remainingTickets; }
    }
}