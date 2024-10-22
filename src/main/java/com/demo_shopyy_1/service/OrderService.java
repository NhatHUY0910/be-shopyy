package com.demo_shopyy_1.service;

import com.demo_shopyy_1.entity.Order;
import com.demo_shopyy_1.dto.CreateOrderRequestDto;
import com.demo_shopyy_1.dto.OrderResponseDto;
import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;

public interface OrderService {
    OrderResponseDto createOrder(CreateOrderRequestDto createOrderRequestDto, HttpServletRequest request) throws UnsupportedEncodingException;
    String generateOrderCode();
    OrderResponseDto convertToOrderResponseDto(Order order);
}
