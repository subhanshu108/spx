package com.amdocs.spx.controller;

import com.amdocs.spx.dto.OrderDTO;
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
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;


    @PostMapping("/createOrder")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            OrderDTO orderDTO;
            if (request.getPaymentMethod() != null) {
                orderDTO = orderService.createOrder(request.getBookingId(), request.getPaymentMethod());
            } else {
                orderDTO = orderService.createOrder(request.getBookingId());
            }
            return new ResponseEntity<>(orderDTO, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteOrder/{id}")
    public String deleteOrder(@PathVariable Long id) {
        return orderService.deleteOrder(id);
    }



    @PutMapping("/editOrder/{id}")
    public OrderDTO editOrder(@PathVariable Long id, @RequestBody OrderDTO orderDTO) {
        return orderService.editOrder(id,orderDTO);
    }
    /**
     * Get order details
     */
    @GetMapping("/getOrderById/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        try {
            OrderDTO order = orderService.getOrderById(orderId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAllOrders")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return orderService.getAllOrders();
    }


    /**
     * Find order by order number
     */
    @PostMapping("/number")
    public ResponseEntity<OrderDTO> getOrderByNumber(@RequestBody OrderNumberRequest request) {
        try {
            OrderDTO order = orderService.getOrderByNumber(request.getOrderNumber());
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
    public ResponseEntity<List<OrderDTO>> getUserOrders(@RequestBody UserRequest request) {
        try {
            List<OrderDTO> orders = orderService.getUserOrders(request.getUserId());
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }








    @PutMapping("/{orderId}/payment-status")
    public ResponseEntity<OrderDTO> updatePaymentStatus(@PathVariable Long orderId, @RequestBody PaymentStatusRequest request) {
        try {
            OrderDTO updatedOrder = orderService.updatePaymentStatus(orderId, request.getPaymentStatus());
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
    public ResponseEntity<OrderDTO> processPayment(@PathVariable Long orderId, @RequestBody ProcessPaymentRequest request) {
        try {
            OrderDTO processedOrder = orderService.processPayment(orderId, request.getPaymentMethod(), request.getTransactionId());
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


}