package com.demo_shopyy_1.dto;

import com.demo_shopyy_1.entity.Cart;
import com.demo_shopyy_1.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CartDtoConverter {

    public CartDto convertToDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setItems(cart.getCartItems().stream()
                .map(this::convertToCartItemDto)
                .collect(Collectors.toList()));
        return dto;
    }

    public CartItemDto convertToCartItemDto(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getProduct().getPrice());
        dto.setImageUrl(item.getProduct().getDefaultImageUrl());
        dto.setStockQuantity(item.getProduct().getStockQuantity());

        // Thêm thông tin về phân loại
        if (item.getSelectedColor() != null) {
            dto.setSelectedColorName(item.getSelectedColor().getName());
        }
        dto.setSelectedSize(item.getSelectedSize());
        dto.setSelectedWeight(item.getSelectedWeight());

        return dto;
    }
}