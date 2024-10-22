package com.demo_shopyy_1.dto;

import com.demo_shopyy_1.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderDto {
    private String fullName;
    private String phoneNumber;
    private String address;
    private Order.PaymentMethod paymentMethod;
    private List<OrderItemDto> orderItems;
}
