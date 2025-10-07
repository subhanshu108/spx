package com.amdocs.spx.repository;

import com.amdocs.spx.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
