package com.amdocs.spx.repository;

import com.amdocs.spx.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
}
