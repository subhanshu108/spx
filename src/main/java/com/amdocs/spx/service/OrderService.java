package com.amdocs.spx.service;

import com.amdocs.spx.dto.OrderDTO;
import com.amdocs.spx.entity.Booking;
import com.amdocs.spx.entity.User;
import com.amdocs.spx.exception.ResourceNotFoundException;
import com.amdocs.spx.mapper.OrderMapper;
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
import java.util.stream.Collectors;

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

    @Autowired
    private OrderMapper orderMapper;

    /**
     * Create order from booking
     */
    public OrderDTO createOrder(Long bookingId) {
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

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDTO(savedOrder);
    }

    /**
     * Create order with payment method
     */
    public OrderDTO createOrder(Long bookingId, String paymentMethod) {
        OrderDTO orderDTO = createOrder(bookingId);
        
        // Get the order entity to update payment method
        Order order = orderRepository.findById(orderDTO.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        order.setPaymentMethod(paymentMethod);
        Order updatedOrder = orderRepository.save(order);
        
        return orderMapper.toDTO(updatedOrder);
    }

    /**
     * Get order details
     */
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toDTO(order);
    }

    /**
     * Find order by order number
     */
    public OrderDTO getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        return orderMapper.toDTO(order);
    }

    /**
     * Get all orders for a user
     */
    public List<OrderDTO> getUserOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        List<Order> orders = orderRepository.findByUser(user);
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update payment status
     */
    public OrderDTO updatePaymentStatus(Long orderId, String paymentStatus) {
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

        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toDTO(updatedOrder);
    }

    /**
     * Handle payment processing
     */
    public OrderDTO processPayment(Long orderId, String paymentMethod, String transactionId) {
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

        Order processedOrder = orderRepository.save(order);
        return orderMapper.toDTO(processedOrder);
    }

    /**
     * Confirm successful payment
     */
    public OrderDTO confirmPayment(Long orderId, String transactionId) {
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

        Order confirmedOrder = orderRepository.save(order);
        return orderMapper.toDTO(confirmedOrder);
    }

    /**
     * Mark payment as failed
     */
    public OrderDTO markPaymentFailed(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if ("COMPLETED".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("Cannot mark completed payment as failed");
        }

        order.setPaymentStatus("FAILED");

        Order failedOrder = orderRepository.save(order);
        return orderMapper.toDTO(failedOrder);
    }

    /**
     * Process refund
     */
    public OrderDTO refundOrder(Long orderId, String reason) {
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

        Order refundedOrder = orderRepository.save(order);
        return orderMapper.toDTO(refundedOrder);
    }

    /**
     * Get user's order history
     */
    public List<OrderDTO> getOrderHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get orders by payment status
     */
    public List<OrderDTO> getOrdersByPaymentStatus(String paymentStatus) {
        List<Order> orders = orderRepository.findByPaymentStatus(paymentStatus.toUpperCase());
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user orders by payment status
     */
    public List<OrderDTO> getUserOrdersByPaymentStatus(Long userId, String paymentStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        List<Order> orders = orderRepository.findByUserAndPaymentStatus(user, paymentStatus.toUpperCase());
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get pending orders (for cleanup/expiry)
     */
    public List<OrderDTO> getPendingOrders() {
        List<Order> orders = orderRepository.findByPaymentStatus("PENDING");
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get orders created between dates
     */
    public List<OrderDTO> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        return orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
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
    public OrderDTO retryPayment(Long orderId) {
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

        Order retriedOrder = orderRepository.save(order);
        return orderMapper.toDTO(retriedOrder);
    }
}