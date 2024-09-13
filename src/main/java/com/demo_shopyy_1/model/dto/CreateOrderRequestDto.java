package com.demo_shopyy_1.model.dto;

import com.demo_shopyy_1.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequestDto {
    private String fullName;
    private String phoneNumber;
    private String address;
//    private List<Long> productIds;
//    private List<Integer> quantities;
    private Order.PaymentMethod paymentMethod;
    private List<OrderItemDto> orderItems;
}
