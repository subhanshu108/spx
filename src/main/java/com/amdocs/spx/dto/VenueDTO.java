package com.amdocs.spx.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenueDTO {


    private Long venueId;
    private String venueName;
    private String venueAddress;
    private String city;
    private int totalCapacity;

}
