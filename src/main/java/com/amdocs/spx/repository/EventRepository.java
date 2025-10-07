package com.amdocs.spx.repository;

import com.amdocs.spx.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
