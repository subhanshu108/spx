package com.amdocs.spx.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDTO {

    private Long eventId;
    private String eventName;

    private String category;
    private LocalDateTime eventDate;
    private String status;
    private String bannerImageUrl;
    private Integer totalTicketsAvailable;
    private Integer ticketsSold;

    // Nested objects
    private VenueDTO venue;
    private OrganizerDTO organizer;
    private List<TicketTypeDTO> ticketTypes;

    // Additional computed fields
    private Integer ticketsRemaining;
    private Double averageRating;
    private Integer totalReviews;

    // Nested DTO for Venue
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VenueDTO {
        private Long venueId;
        private String venueName;
        private String address;
        private String city;
        private String state;
        private String country;
        private Integer capacity;
    }

    // Nested DTO for Organizer (User)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizerDTO {
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;
        private String organizationName;
    }

    // Nested DTO for Ticket Types
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketTypeDTO {
        private Long ticketTypeId;
        private String typeName;
        private String description;
        private Double price;
        private Integer quantityAvailable;
        private Integer quantitySold;
        private Boolean isActive;
    }

    // Convenience method to calculate remaining tickets
    public Integer getTicketsRemaining() {
        if (totalTicketsAvailable != null && ticketsSold != null) {
            return totalTicketsAvailable - ticketsSold;
        }
        return ticketsRemaining;
    }
}