package com.demo_shopyy_1.dto;

import com.demo_shopyy_1.entity.Order;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String paymentUrl;
}
