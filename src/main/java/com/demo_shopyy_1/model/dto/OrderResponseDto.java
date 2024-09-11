package com.demo_shopyy_1.model.dto;

import com.demo_shopyy_1.model.Order;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String address;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private Order.PaymentMethod paymentMethod;
    private String paymentTransactionId;
    private List<OrderItemDto> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}