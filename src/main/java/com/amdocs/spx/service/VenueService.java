package com.amdocs.spx.service;


import com.amdocs.spx.entity.Venue;
import com.amdocs.spx.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VenueService {

    @Autowired
    private VenueRepository venueRepository;

    public Venue createVenue(Venue venue) {
        return venueRepository.save(venue);
    }
    public Venue updateVenue(Venue venue) {
        return venueRepository.save(venue);
    }
    public String deleteVenue(Venue venue) {
        venueRepository.delete(venue);
        return "Venue deleted successfully";
    }
    public Optional<Venue> getVenueById(Long venueId) {
        return venueRepository.findById(venueId);
    }
    public List<Venue> getAllVenue() {
        return venueRepository.findAll();
    }

}
