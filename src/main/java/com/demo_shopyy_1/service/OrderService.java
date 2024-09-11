package com.demo_shopyy_1.service;

import com.demo_shopyy_1.model.Order;
import com.demo_shopyy_1.model.User;
import com.demo_shopyy_1.model.dto.CreateOrderDto;
import com.demo_shopyy_1.model.dto.CreateOrderRequestDto;
import com.demo_shopyy_1.model.dto.OrderResponseDto;
import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;

public interface OrderService {
    OrderResponseDto createOrder(CreateOrderRequestDto createOrderRequestDto, HttpServletRequest request) throws UnsupportedEncodingException;
    String generateOrderCode();
    OrderResponseDto convertToOrderResponseDto(Order order);
}
