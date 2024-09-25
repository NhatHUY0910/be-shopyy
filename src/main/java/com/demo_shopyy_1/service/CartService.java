package com.demo_shopyy_1.service;

import com.demo_shopyy_1.model.User;
import com.demo_shopyy_1.model.dto.AddToCartDto;
import com.demo_shopyy_1.model.dto.CartDto;

public interface CartService {
    CartDto addToCart(User user, AddToCartDto addToCartDto);
    CartDto getCartForUser(User user);
    CartDto updateCartItemQuantity(User user, AddToCartDto updateCartDto);
    CartDto removeCartItem(User user, Long productId);
    CartDto removeAllCartItems(User user);
}
