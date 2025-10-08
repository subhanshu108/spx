package com.amdocs.spx.service;


import com.amdocs.spx.entity.Order;
import com.amdocs.spx.entity.OrderItem;
import com.amdocs.spx.entity.TicketType;
import com.amdocs.spx.repository.OrderItemRepository;
import com.amdocs.spx.repository.OrderRepository;
import com.amdocs.spx.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final TicketTypeRepository ticketTypeRepository;

    /**
     * Create a new order item and add it to an order
     */
    public OrderItem createOrderItem(Long orderId, Long ticketTypeId, Integer quantity) {
        // Validate order exists
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Validate ticket type exists
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new RuntimeException("Ticket type not found with ID: " + ticketTypeId));

        // Validate ticket type is active
        if (!ticketType.getIsActive()) {
            throw new RuntimeException("Ticket type is not active");
        }

        // Validate quantity
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        // Check availability
        int availableQuantity = ticketType.getQuantityAvailable() - ticketType.getQuantitySold();
        if (quantity > availableQuantity) {
            throw new RuntimeException("Insufficient tickets available. Available: " + availableQuantity);
        }

        // Create order item
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setTicketType(ticketType);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(ticketType.getPrice());

        // Calculate subtotal
        BigDecimal subtotal = calculateSubtotal(ticketType.getPrice(), quantity);
        orderItem.setSubtotal(subtotal);

        // Save order item
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);

        // Update ticket type sold quantity
        ticketType.setQuantitySold(ticketType.getQuantitySold() + quantity);
        ticketTypeRepository.save(ticketType);

        // Update order total
        updateOrderTotal(order);

        return savedOrderItem;
    }

    /**
     * Get order item by ID
     */
    @Transactional(readOnly = true)
    public OrderItem getOrderItemById(Long orderItemId) {
        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found with ID: " + orderItemId));
    }

    /**
     * Get all items in an order
     */
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrder(Long orderId) {
        // Validate order exists
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }

        return orderItemRepository.findByOrder_OrderId(orderId);
    }

    /**
     * Update an existing order item
     */
    public OrderItem updateOrderItem(Long orderItemId, Integer newQuantity) {
        // Validate quantity
        if (newQuantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        // Fetch existing order item
        OrderItem orderItem = getOrderItemById(orderItemId);
        TicketType ticketType = orderItem.getTicketType();
        int oldQuantity = orderItem.getQuantity();
        int quantityDifference = newQuantity - oldQuantity;

        // Check availability if increasing quantity
        if (quantityDifference > 0) {
            int availableQuantity = ticketType.getQuantityAvailable() - ticketType.getQuantitySold();
            if (quantityDifference > availableQuantity) {
                throw new RuntimeException("Insufficient tickets available. Available: " + availableQuantity);
            }
        }

        // Update order item
        orderItem.setQuantity(newQuantity);
        BigDecimal newSubtotal = calculateSubtotal(orderItem.getUnitPrice(), newQuantity);
        orderItem.setSubtotal(newSubtotal);

        // Update ticket type sold quantity
        ticketType.setQuantitySold(ticketType.getQuantitySold() + quantityDifference);
        ticketTypeRepository.save(ticketType);

        // Save updated order item
        OrderItem updatedOrderItem = orderItemRepository.save(orderItem);

        // Update order total
        updateOrderTotal(orderItem.getOrder());

        return updatedOrderItem;
    }

    /**
     * Delete an order item
     */
    public void deleteOrderItem(Long orderItemId) {
        // Fetch order item
        OrderItem orderItem = getOrderItemById(orderItemId);
        Order order = orderItem.getOrder();
        TicketType ticketType = orderItem.getTicketType();

        // Restore ticket quantity
        ticketType.setQuantitySold(ticketType.getQuantitySold() - orderItem.getQuantity());
        ticketTypeRepository.save(ticketType);

        // Delete order item
        orderItemRepository.delete(orderItem);

        // Update order total
        updateOrderTotal(order);
    }

    /**
     * Calculate subtotal for an order item
     */
    public BigDecimal calculateSubtotal(BigDecimal unitPrice, Integer quantity) {
        if (unitPrice == null || quantity == null || quantity <= 0) {
            throw new RuntimeException("Invalid parameters for subtotal calculation");
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Helper method to update order total amount
     */
    private void updateOrderTotal(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrder_OrderId(order.getOrderId());

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
    }
}
