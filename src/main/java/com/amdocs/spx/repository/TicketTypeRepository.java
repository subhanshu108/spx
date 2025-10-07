package com.amdocs.spx.repository;

import com.amdocs.spx.entity.Event;
import com.amdocs.spx.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByEvent(Event event);

    List<TicketType> findByEventAndIsActive(Event event, boolean b);
}
