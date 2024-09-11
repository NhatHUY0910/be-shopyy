package com.demo_shopyy_1.controller;

import com.demo_shopyy_1.model.Order;
import com.demo_shopyy_1.model.dto.CreateOrderDto;
import com.demo_shopyy_1.model.dto.CreateOrderRequestDto;
import com.demo_shopyy_1.model.dto.OrderResponseDto;
import com.demo_shopyy_1.service.IUserService;
import com.demo_shopyy_1.service.OrderService;
import com.demo_shopyy_1.service.impl.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private IUserService userService;

    @Autowired
    private VNPayService vnPayService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody CreateOrderRequestDto requestDto,
                                                        HttpServletRequest request) {
        try {
            OrderResponseDto responseDto = orderService.createOrder(requestDto, request);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.badRequest().body(null);
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
