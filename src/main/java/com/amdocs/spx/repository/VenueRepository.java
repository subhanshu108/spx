package com.amdocs.spx.repository;

import com.amdocs.spx.Entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
