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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
        User user = userRepository.findById(booking.getUser().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + booking.getUser().getUserId()));
        if(!booking.getOrders().isEmpty()) {
            throw new ResourceNotFoundException("Orders already exist");
        }
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setBooking(booking);
        order.setUser(user);
        order.setTotalAmount(booking.getTotalAmount());
        order.setPaymentStatus("CONFIRMED");
        order.setCreatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        booking.setBookingStatus("CONFIRMED");
        bookingRepository.save(booking);
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











    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return ResponseEntity.ok(orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList()));
    }

    public String deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        orderRepository.deleteById(id);
        return "Order deleted Successfully";
    }


    public OrderDTO editOrder(Long id, OrderDTO orderDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Only update fields that are not null in the DTO
        if (orderDTO.getOrderNumber() != null) {
            order.setOrderNumber(orderDTO.getOrderNumber());
        }
        if (orderDTO.getTotalAmount() != null) {
            order.setTotalAmount(orderDTO.getTotalAmount());
        }
        if (orderDTO.getPaymentStatus() != null) {
            order.setPaymentStatus(orderDTO.getPaymentStatus());
        }
        if (orderDTO.getCreatedAt() != null) {
            order.setCreatedAt(orderDTO.getCreatedAt());
        }
        if (orderDTO.getBookingId() != null) {
            Booking booking = bookingRepository.findById(orderDTO.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + orderDTO.getBookingId()));
            order.setBooking(booking);
        }

        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toDTO(updatedOrder);
    }
}