package com.amdocs.spx.repository;

import com.amdocs.spx.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
