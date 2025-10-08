package com.amdocs.spx.controller;

import com.amdocs.spx.entity.OrderItem;
import com.amdocs.spx.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("api/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    /**
     * Add item to order
     */
    @PostMapping
    public ResponseEntity<OrderItem> createOrderItem(@RequestBody CreateOrderItemRequest request) {
        try {
            OrderItem orderItem = orderItemService.createOrderItem(
                request.getOrderId(), 
                request.getTicketTypeId(), 
                request.getQuantity()
            );
            return new ResponseEntity<>(orderItem, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            } else if (e.getMessage().contains("not active") || 
                       e.getMessage().contains("must be greater than 0") ||
                       e.getMessage().contains("Insufficient tickets")) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get order item details
     */
    @GetMapping("/{orderItemId}")
    public ResponseEntity<OrderItem> getOrderItemById(@PathVariable Long orderItemId) {
        try {
            OrderItem orderItem = orderItemService.getOrderItemById(orderItemId);
            return new ResponseEntity<>(orderItem, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all items in an order
     */
    @PostMapping("/order")
    public ResponseEntity<List<OrderItem>> getOrderItemsByOrder(@RequestBody OrderRequest request) {
        try {
            List<OrderItem> orderItems = orderItemService.getOrderItemsByOrder(request.getOrderId());
            return new ResponseEntity<>(orderItems, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Modify order item
     */
    @PutMapping("/{orderItemId}")
    public ResponseEntity<OrderItem> updateOrderItem(@PathVariable Long orderItemId, @RequestBody UpdateOrderItemRequest request) {
        try {
            OrderItem updatedOrderItem = orderItemService.updateOrderItem(orderItemId, request.getQuantity());
            return new ResponseEntity<>(updatedOrderItem, HttpStatus.OK);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            } else if (e.getMessage().contains("must be greater than 0") ||
                       e.getMessage().contains("Insufficient tickets")) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Remove item from order
     */
    @DeleteMapping("/{orderItemId}")
    public ResponseEntity<String> deleteOrderItem(@PathVariable Long orderItemId) {
        try {
            orderItemService.deleteOrderItem(orderItemId);
            return new ResponseEntity<>("Order item deleted successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting order item", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Calculate item subtotal
     */
    @PostMapping("/calculate-subtotal")
    public ResponseEntity<SubtotalResponse> calculateSubtotal(@RequestBody CalculateSubtotalRequest request) {
        try {
            BigDecimal subtotal = orderItemService.calculateSubtotal(request.getUnitPrice(), request.getQuantity());
            SubtotalResponse response = new SubtotalResponse(subtotal);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Request DTO classes for @RequestBody parameters

    public static class CreateOrderItemRequest {
        private Long orderId;
        private Long ticketTypeId;
        private Integer quantity;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public Long getTicketTypeId() { return ticketTypeId; }
        public void setTicketTypeId(Long ticketTypeId) { this.ticketTypeId = ticketTypeId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class OrderRequest {
        private Long orderId;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
    }

    public static class UpdateOrderItemRequest {
        private Integer quantity;

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class CalculateSubtotalRequest {
        private BigDecimal unitPrice;
        private Integer quantity;

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class SubtotalResponse {
        private BigDecimal subtotal;

        public SubtotalResponse(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }

        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }
}