package com.amdocs.spx.repository;

import com.amdocs.spx.entity.Event;
import com.amdocs.spx.entity.User;
import com.amdocs.spx.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCategory(String category);

    List<Event> findByOrganizer(User organizer);

    List<Event> findByEventDateAfterAndStatus(LocalDateTime date, String status);

    List<Event> findByEventNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String eventName, String description);

    List<Event> findByVenue(Venue venue);
}
