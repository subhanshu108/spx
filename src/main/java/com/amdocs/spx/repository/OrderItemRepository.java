package com.amdocs.spx.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.amdocs.spx.entity.OrderItem;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Find all order items for a specific order
    List<OrderItem> findByOrder_OrderId(Long orderId);

    // Find all order items for a specific ticket type
    List<OrderItem> findByTicketType_TicketTypeId(Long ticketTypeId);

    // Delete all order items for a specific order (useful for cascade operations)
    void deleteByOrder_OrderId(Long orderId);
}
