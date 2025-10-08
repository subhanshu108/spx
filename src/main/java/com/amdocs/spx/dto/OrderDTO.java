package com.amdocs.spx.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {
    private Long orderId;
    private String orderNumber;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
    private Long bookingId;
    private Long userId;
}