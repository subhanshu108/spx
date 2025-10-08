package com.amdocs.spx.controller;

import com.amdocs.spx.entity.Order;
import com.amdocs.spx.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Create order from booking
     */
    @PostMapping("/createOrder")
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Order order;
            if (request.getPaymentMethod() != null) {
                order = orderService.createOrder(request.getBookingId(), request.getPaymentMethod());
            } else {
                order = orderService.createOrder(request.getBookingId());
            }
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get order details
     */
    @GetMapping("/getOrderById/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Find order by order number
     */
    @PostMapping("/number")
    public ResponseEntity<Order> getOrderByNumber(@RequestBody OrderNumberRequest request) {
        try {
            Order order = orderService.getOrderByNumber(request.getOrderNumber());
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all orders for a user
     */
    @PostMapping("/user")
    public ResponseEntity<List<Order>> getUserOrders(@RequestBody UserRequest request) {
        try {
            List<Order> orders = orderService.getUserOrders(request.getUserId());
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update payment status
     */
    @PutMapping("/{orderId}/payment-status")
    public ResponseEntity<Order> updatePaymentStatus(@PathVariable Long orderId, @RequestBody PaymentStatusRequest request) {
        try {
            Order updatedOrder = orderService.updatePaymentStatus(orderId, request.getPaymentStatus());
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handle payment processing
     */
    @PutMapping("/{orderId}/process-payment")
    public ResponseEntity<Order> processPayment(@PathVariable Long orderId, @RequestBody ProcessPaymentRequest request) {
        try {
            Order processedOrder = orderService.processPayment(orderId, request.getPaymentMethod(), request.getTransactionId());
            return new ResponseEntity<>(processedOrder, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Confirm successful payment
     */
    @PutMapping("/{orderId}/confirm-payment")
    public ResponseEntity<Order> confirmPayment(@PathVariable Long orderId, @RequestBody ConfirmPaymentRequest request) {
        try {
            Order confirmedOrder = orderService.confirmPayment(orderId, request.getTransactionId());
            return new ResponseEntity<>(confirmedOrder, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Mark payment as failed
     */
    @PutMapping("/{orderId}/mark-failed")
    public ResponseEntity<Order> markPaymentFailed(@PathVariable Long orderId, @RequestBody PaymentFailedRequest request) {
        try {
            Order failedOrder = orderService.markPaymentFailed(orderId, request.getReason());
            return new ResponseEntity<>(failedOrder, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Process refund
     */
    @PutMapping("/{orderId}/refund")
    public ResponseEntity<Order> refundOrder(@PathVariable Long orderId, @RequestBody RefundRequest request) {
        try {
            Order refundedOrder = orderService.refundOrder(orderId, request.getReason());
            return new ResponseEntity<>(refundedOrder, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get user's order history
     */
    @PostMapping("/user/history")
    public ResponseEntity<List<Order>> getOrderHistory(@RequestBody UserRequest request) {
        try {
            List<Order> orderHistory = orderService.getOrderHistory(request.getUserId());
            return new ResponseEntity<>(orderHistory, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get orders by payment status
     */
    @PostMapping("/payment-status")
    public ResponseEntity<List<Order>> getOrdersByPaymentStatus(@RequestBody PaymentStatusRequest request) {
        try {
            List<Order> orders = orderService.getOrdersByPaymentStatus(request.getPaymentStatus());
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get user orders by payment status
     */
    @PostMapping("/user/payment-status")
    public ResponseEntity<List<Order>> getUserOrdersByPaymentStatus(@RequestBody UserPaymentStatusRequest request) {
        try {
            List<Order> orders = orderService.getUserOrdersByPaymentStatus(request.getUserId(), request.getPaymentStatus());
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get pending orders (for admin/cleanup)
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Order>> getPendingOrders() {
        try {
            List<Order> pendingOrders = orderService.getPendingOrders();
            return new ResponseEntity<>(pendingOrders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get orders between dates
     */
    @PostMapping("/date-range")
    public ResponseEntity<List<Order>> getOrdersBetweenDates(@RequestBody DateRangeRequest request) {
        try {
            List<Order> orders = orderService.getOrdersBetweenDates(request.getStartDate(), request.getEndDate());
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get total revenue for a user (as organizer)
     */
    @PostMapping("/user/revenue")
    public ResponseEntity<RevenueResponse> getUserRevenue(@RequestBody UserRequest request) {
        try {
            BigDecimal revenue = orderService.getUserRevenue(request.getUserId());
            RevenueResponse response = new RevenueResponse(revenue);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cancel expired pending orders (admin endpoint)
     */
    @PostMapping("/cancel-expired")
    public ResponseEntity<String> cancelExpiredOrders(@RequestBody CancelExpiredRequest request) {
        try {
            orderService.cancelExpiredOrders(request.getExpiryMinutes());
            return new ResponseEntity<>("Expired orders cancelled successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error cancelling expired orders", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retry failed payment
     */
    @PutMapping("/{orderId}/retry-payment")
    public ResponseEntity<Order> retryPayment(@PathVariable Long orderId) {
        try {
            Order retriedOrder = orderService.retryPayment(orderId);
            return new ResponseEntity<>(retriedOrder, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Request DTO classes for @RequestBody parameters

    public static class CreateOrderRequest {
        private Long bookingId;
        private String paymentMethod;

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    public static class OrderNumberRequest {
        private String orderNumber;

        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    }

    public static class UserRequest {
        private Long userId;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    public static class PaymentStatusRequest {
        private String paymentStatus;

        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    }

    public static class ProcessPaymentRequest {
        private String paymentMethod;
        private String transactionId;

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    }

    public static class ConfirmPaymentRequest {
        private String transactionId;

        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    }

    public static class PaymentFailedRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class RefundRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class UserPaymentStatusRequest {
        private Long userId;
        private String paymentStatus;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    }

    public static class DateRangeRequest {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime startDate;
        
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime endDate;

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    }

    public static class RevenueResponse {
        private BigDecimal revenue;

        public RevenueResponse(BigDecimal revenue) {
            this.revenue = revenue;
        }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    }

    public static class CancelExpiredRequest {
        private Integer expiryMinutes;

        public Integer getExpiryMinutes() { return expiryMinutes; }
        public void setExpiryMinutes(Integer expiryMinutes) { this.expiryMinutes = expiryMinutes; }
    }
}