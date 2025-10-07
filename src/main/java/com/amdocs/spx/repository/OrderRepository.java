package com.amdocs.spx.repository;

import com.amdocs.spx.entity.Order;
import com.amdocs.spx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUser(User user);

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    List<Order> findByPaymentStatus(String paymentStatus);

    List<Order> findByUserAndPaymentStatus(User user, String paymentStatus);

    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByPaymentStatusAndCreatedAtBefore(String paymentStatus, LocalDateTime createdAt);
}
