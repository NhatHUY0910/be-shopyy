package com.demo_shopyy_1.controller;

import com.demo_shopyy_1.model.dto.CreateOrderRequestDto;
import com.demo_shopyy_1.model.dto.OrderResponseDto;
import com.demo_shopyy_1.service.UserService;
import com.demo_shopyy_1.service.OrderService;
import com.demo_shopyy_1.service.impl.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private VNPayService vnPayService;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequestDto requestDto,
                                         HttpServletRequest request) {
        log.info("Received order request: {}", requestDto);
        try {
            OrderResponseDto responseDto = orderService.createOrder(requestDto, request);
            log.info("Order created successfully: {}", responseDto);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            log.error("Invalid input data", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> vnpayParams) {
        boolean paymentProcessed = vnPayService.processPaymentReturn(vnpayParams);
        if (paymentProcessed) {
            return ResponseEntity.ok("Payment processed successfully");
        } else {
            return ResponseEntity.badRequest().body("Payment processing failed");
        }
    }
}
