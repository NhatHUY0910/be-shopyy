package com.demo_shopyy_1.controller;

import com.demo_shopyy_1.model.dto.CartDtoConverter;
import com.demo_shopyy_1.model.User;
import com.demo_shopyy_1.model.dto.AddToCartDto;
import com.demo_shopyy_1.model.dto.CartDto;
import com.demo_shopyy_1.service.ICartService;
import com.demo_shopyy_1.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
//@CrossOrigin(origins = "http://localhost:63342")
public class CartController {

    @Autowired
    private ICartService cartService;

    @Autowired
    private IUserService userService;

    @Autowired
    private CartDtoConverter cartDtoConverter;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartDto addToCartDto) {
        try {
            User user = userService.getCurrentUser();
            CartDto updatedCart = cartService.addToCart(user, addToCartDto);
            return ResponseEntity.ok(updatedCart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getCart() {
        try {
            User user = userService.getCurrentUser();
            CartDto cartDto = cartService.getCartForUser(user);
            return ResponseEntity.ok(cartDto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateCartItem(@RequestBody AddToCartDto updatedCartDto) {
        try {
            User user = userService.getCurrentUser();
            CartDto updateCart = cartService.updateCartItemQuantity(user, updatedCartDto);
            return ResponseEntity.ok(updateCart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeCartItem(@RequestBody AddToCartDto removeCartDto) {
        try {
            User user = userService.getCurrentUser();
            CartDto updateCart = cartService.removeCartItem(user, removeCartDto.getProductId());
            return ResponseEntity.ok(updateCart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/remove-all")
    public ResponseEntity<?> removeAllCartItems() {
        try {
            User user = userService.getCurrentUser();
            CartDto updateCart = cartService.removeAllCartItems(user);
            return ResponseEntity.ok(updateCart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
