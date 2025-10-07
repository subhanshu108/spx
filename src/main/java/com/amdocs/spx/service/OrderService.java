package com.amdocs.spx.service;


import com.amdocs.spx.entity.Booking;
import com.amdocs.spx.entity.User;
import com.amdocs.spx.exception.ResourceNotFoundException;
import com.amdocs.spx.repository.BookingRepository;
import com.amdocs.spx.repository.OrderRepository;
import com.amdocs.spx.repository.UserRepository;
import com.amdocs.spx.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingService bookingService;

    /**
     * Create order from booking
     */
    public Order createOrder(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // Check if booking already has an order
        if (!booking.getOrders().isEmpty()) {
            throw new IllegalStateException("Order already exists for this booking");
        }

        // Validate booking is in pending status
        if (!"PENDING".equals(booking.getBookingStatus())) {
            throw new IllegalStateException("Can only create order for pending bookings");
        }

        // Validate booking is still valid
        if (!bookingService.validateBooking(bookingId)) {
            throw new IllegalStateException("Booking is no longer valid");
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setBooking(booking);
        order.setUser(booking.getUser());
        order.setTotalAmount(booking.getTotalAmount());
        order.setPaymentStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    /**
     * Create order with payment method
     */
    public Order createOrder(Long bookingId, String paymentMethod) {
        Order order = createOrder(bookingId);
        order.setPaymentMethod(paymentMethod);
        return orderRepository.save(order);
    }

    /**
     * Get order details
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    /**
     * Find order by order number
     */
    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
    }

    /**
     * Get all orders for a user
     */
    public List<Order> getUserOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return orderRepository.findByUser(user);
    }

    /**
     * Update payment status
     */
    public Order updatePaymentStatus(Long orderId, String paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Validate payment status
        List<String> validStatuses = Arrays.asList("PENDING", "PROCESSING", "COMPLETED", "FAILED", "REFUNDED");
        if (!validStatuses.contains(paymentStatus.toUpperCase())) {
            throw new IllegalArgumentException("Invalid payment status. Valid statuses are: " + validStatuses);
        }

        order.setPaymentStatus(paymentStatus.toUpperCase());

        // If payment is completed, update payment date
        if ("COMPLETED".equals(paymentStatus.toUpperCase())) {
            order.setPaymentDate(LocalDateTime.now());
            // Confirm the associated booking
            bookingService.confirmBooking(order.getBooking().getBookingId());
        }

        return orderRepository.save(order);
    }

    /**
     * Handle payment processing
     */
    public Order processPayment(Long orderId, String paymentMethod, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Check if order is in pending status
        if (!"PENDING".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("Can only process payment for pending orders");
        }

        // Validate payment method
        List<String> validPaymentMethods = Arrays.asList("CREDIT_CARD", "DEBIT_CARD", "UPI", "NET_BANKING", "WALLET");
        if (!validPaymentMethods.contains(paymentMethod.toUpperCase())) {
            throw new IllegalArgumentException("Invalid payment method. Valid methods are: " + validPaymentMethods);
        }

        order.setPaymentMethod(paymentMethod.toUpperCase());
        order.setTransactionId(transactionId);
        order.setPaymentStatus("PROCESSING");

        return orderRepository.save(order);
    }

    /**
     * Confirm successful payment
     */
    public Order confirmPayment(Long orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Check if order is in processing status
        if (!"PROCESSING".equals(order.getPaymentStatus()) && !"PENDING".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("Can only confirm payment for processing or pending orders");
        }

        order.setTransactionId(transactionId);
        order.setPaymentStatus("COMPLETED");
        order.setPaymentDate(LocalDateTime.now());

        // Confirm the associated booking
        bookingService.confirmBooking(order.getBooking().getBookingId());

        return orderRepository.save(order);
    }

    /**
     * Mark payment as failed
     */
    public Order markPaymentFailed(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if ("COMPLETED".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("Cannot mark completed payment as failed");
        }

        order.setPaymentStatus("FAILED");

        return orderRepository.save(order);
    }

    /**
     * Process refund
     */
    public Order refundOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Check if payment was completed
        if (!"COMPLETED".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("Can only refund completed payments");
        }

        // Check if already refunded
        if ("REFUNDED".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("Order is already refunded");
        }

        // Cancel the associated booking
        bookingService.cancelBooking(order.getBooking().getBookingId());

        order.setPaymentStatus("REFUNDED");

        return orderRepository.save(order);
    }

    /**
     * Get user's order history
     */
    public List<Order> getOrderHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Get orders by payment status
     */
    public List<Order> getOrdersByPaymentStatus(String paymentStatus) {
        return orderRepository.findByPaymentStatus(paymentStatus.toUpperCase());
    }

    /**
     * Get user orders by payment status
     */
    public List<Order> getUserOrdersByPaymentStatus(Long userId, String paymentStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return orderRepository.findByUserAndPaymentStatus(user, paymentStatus.toUpperCase());
    }

    /**
     * Get pending orders (for cleanup/expiry)
     */
    public List<Order> getPendingOrders() {
        return orderRepository.findByPaymentStatus("PENDING");
    }

    /**
     * Get orders created between dates
     */
    public List<Order> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCreatedAtBetween(startDate, endDate);
    }

    /**
     * Get total revenue for a user (as organizer)
     */
    public BigDecimal getUserRevenue(Long userId) {
        List<Order> completedOrders = orderRepository.findByUserAndPaymentStatus(
                userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found")),
                "COMPLETED"
        );

        return completedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Cancel expired pending orders
     */
    public void cancelExpiredOrders(int expiryMinutes) {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(expiryMinutes);
        List<Order> expiredOrders = orderRepository.findByPaymentStatusAndCreatedAtBefore("PENDING", expiryTime);

        for (Order order : expiredOrders) {
            try {
                order.setPaymentStatus("FAILED");
                orderRepository.save(order);
                // Cancel the associated booking
                bookingService.cancelBooking(order.getBooking().getBookingId());
            } catch (Exception e) {
                // Log error but continue processing other orders
                System.err.println("Failed to cancel order: " + order.getOrderNumber() + " - " + e.getMessage());
            }
        }
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        String orderNumber;
        do {
            // Generate format: ORD-YYYYMMDD-XXXXXX (random 6 digit number)
            String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String randomPart = String.format("%06d", (int) (Math.random() * 1000000));
            orderNumber = "ORD-" + datePart + "-" + randomPart;
        } while (orderRepository.findByOrderNumber(orderNumber).isPresent());

        return orderNumber;
    }

    /**
     * Retry failed payment
     */
    public Order retryPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!"FAILED".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("Can only retry failed payments");
        }

        // Validate booking is still valid
        if (!bookingService.validateBooking(order.getBooking().getBookingId())) {
            throw new IllegalStateException("Booking is no longer valid");
        }

        order.setPaymentStatus("PENDING");
        order.setTransactionId(null);

        return orderRepository.save(order);
    }
}