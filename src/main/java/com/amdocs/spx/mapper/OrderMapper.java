package com.amdocs.spx.mapper;

import com.amdocs.spx.dto.OrderDTO;
import com.amdocs.spx.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    
    public OrderDTO toDTO(Order order) {
        if (order == null) return null;
        
        return new OrderDTO(
            order.getOrderId(),
            order.getOrderNumber(),
            order.getTotalAmount(),
            order.getPaymentStatus(),
            order.getPaymentMethod(),
            order.getTransactionId(),
            order.getPaymentDate(),
            order.getCreatedAt(),
            order.getBooking() != null ? order.getBooking().getBookingId() : null,
            order.getUser() != null ? order.getUser().getUserId() : null
        );
    }
    
    public Order toEntity(OrderDTO orderDTO) {
        if (orderDTO == null) return null;
        
        Order order = new Order();
        order.setOrderId(orderDTO.getOrderId());
        order.setOrderNumber(orderDTO.getOrderNumber());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setPaymentStatus(orderDTO.getPaymentStatus());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setTransactionId(orderDTO.getTransactionId());
        order.setPaymentDate(orderDTO.getPaymentDate());
        order.setCreatedAt(orderDTO.getCreatedAt());
        // Note: booking and user would be set separately via service
        return order;
    }
}