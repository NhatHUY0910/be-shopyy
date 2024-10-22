package com.demo_shopyy_1.service;

import com.demo_shopyy_1.entity.User;
import com.demo_shopyy_1.dto.AddToCartDto;
import com.demo_shopyy_1.dto.CartDto;

public interface CartService {
    CartDto addToCart(User user, AddToCartDto addToCartDto);
    CartDto getCartForUser(User user);
    CartDto updateCartItemQuantity(User user, AddToCartDto updateCartDto);
    CartDto removeCartItem(User user, Long productId);
    CartDto removeAllCartItems(User user);
}
