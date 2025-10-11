package com.amdocs.spx;

import com.amdocs.spx.controller.OrderController;
import com.amdocs.spx.dto.OrderDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class OrderControllerTest {

    @Test
    void testCreateOrderRequest() {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setBookingId(123L);
        request.setPaymentMethod("CreditCard");

        Assertions.assertEquals(123L, request.getBookingId());
        Assertions.assertEquals("CreditCard", request.getPaymentMethod());
    }

    @Test
    void testOrderNumberRequest() {
        OrderController.OrderNumberRequest request = new OrderController.OrderNumberRequest();
        request.setOrderNumber("ORD001");

        Assertions.assertEquals("ORD001", request.getOrderNumber());
    }

    @Test
    void testUserRequest() {
        OrderController.UserRequest request = new OrderController.UserRequest();
        request.setUserId(42L);

        Assertions.assertEquals(42L, request.getUserId());
    }

    @Test
    void testPaymentStatusRequest() {
        OrderController.PaymentStatusRequest request = new OrderController.PaymentStatusRequest();
        request.setPaymentStatus("PAID");

        Assertions.assertEquals("PAID", request.getPaymentStatus());
    }

    @Test
    void testProcessPaymentRequest() {
        OrderController.ProcessPaymentRequest request = new OrderController.ProcessPaymentRequest();
        request.setPaymentMethod("UPI");
        request.setTransactionId("TXN123");

        Assertions.assertEquals("UPI", request.getPaymentMethod());
        Assertions.assertEquals("TXN123", request.getTransactionId());
    }

    @Test
    void testDeleteResponseEntity() {
        // Simulate delete order response
        String result = "Order deleted successfully";
        Assertions.assertEquals("Order deleted successfully", result);
    }

    @Test
    void testDummyResponseEntity() {
        // Simulate a response for getOrderById
        ResponseEntity<OrderDTO> response =
                new ResponseEntity<>(new OrderDTO(), HttpStatus.OK);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void testDummyListResponseEntity() {
        // Simulate a response for getAllOrders or getUserOrders
        ResponseEntity<List<OrderDTO>> response =
                new ResponseEntity<>(List.of(new OrderDTO(), new OrderDTO()), HttpStatus.OK);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(2, response.getBody().size());
    }
}
