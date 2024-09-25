package com.demo_shopyy_1.service.impl;

import com.demo_shopyy_1.model.dto.CartDtoConverter;
import com.demo_shopyy_1.model.Cart;
import com.demo_shopyy_1.model.CartItem;
import com.demo_shopyy_1.model.Product;
import com.demo_shopyy_1.model.User;
import com.demo_shopyy_1.model.dto.AddToCartDto;
import com.demo_shopyy_1.model.dto.CartDto;
import com.demo_shopyy_1.repository.CartRepository;
import com.demo_shopyy_1.repository.ProductRepository;
import com.demo_shopyy_1.repository.UserRepository;
import com.demo_shopyy_1.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartDtoConverter cartDtoConverter;

    @Override
    @Transactional
    public CartDto addToCart(User user, AddToCartDto addToCartDto) {
        Product product = productRepository.findById(addToCartDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (addToCartDto.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        if (addToCartDto.getQuantity() > product.getStockQuantity()){
            throw new RuntimeException("Quantity must be less than or equal to product's quantity");
        }

        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
            user.setCart(cart);
            userRepository.save(user);
//            throw new RuntimeException("User don't have a cart");
        }

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        int quantityToAdd = addToCartDto.getQuantity();

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(addToCartDto.getQuantity());
//            cart.getCartItems().add(cartItem);
            cart.addCartItem(cartItem);
        } else {
            int newQuantity = cartItem.getQuantity() + addToCartDto.getQuantity();
            if (newQuantity > product.getStockQuantity()){
                throw new RuntimeException("Quantity is greater than stock");
            }
            cartItem.setQuantity(newQuantity);
        }

        product.setStockQuantity(product.getStockQuantity() - quantityToAdd);
        productRepository.save(product);

        Cart savedCart = cartRepository.save(cart);
        return cartDtoConverter.convertToDto(savedCart);
    }

    @Override
    public CartDto getCartForUser(User user) {
        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            user.setCart(cart);
            cart = cartRepository.save(cart);
        }
        return cartDtoConverter.convertToDto(cart);
    }

    @Override
    @Transactional
    public CartDto updateCartItemQuantity(User user, AddToCartDto updateCartDto) {
        Cart cart = user.getCart();
        if (cart == null) {
            throw new RuntimeException("Cart not found for user");
        }

        Product product = productRepository.findById(updateCartDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        int quantityDifference = updateCartDto.getQuantity() - cartItem.getQuantity();

        int availableStock = product.getStockQuantity() + cartItem.getQuantity();
        if (updateCartDto.getQuantity() > availableStock) {
            throw new RuntimeException("Not enough stock available. Maximum available quantity is " + availableStock);
        }

        cartItem.setQuantity(updateCartDto.getQuantity());
        product.setStockQuantity(product.getStockQuantity() - quantityDifference);

        productRepository.save(product);
        Cart savedCart = cartRepository.save(cart);
        return cartDtoConverter.convertToDto(savedCart);
    }

    @Override
    @Transactional
    public CartDto removeCartItem(User user, Long productId) {
        Cart cart = user.getCart();
        if (cart == null) {
            throw new RuntimeException("Cart not found for user");
        }

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        Product product = cartItem.getProduct();
        product.setStockQuantity(product.getStockQuantity() + cartItem.getQuantity());
        productRepository.save(product);

        cart.getCartItems().remove(cartItem);
        Cart savedCart = cartRepository.save(cart);
        return cartDtoConverter.convertToDto(savedCart);
    }

    @Override
    @Transactional
    public CartDto removeAllCartItems(User user) {
        Cart cart = user.getCart();
        if (cart == null) {
            throw new RuntimeException("Cart not found for user");
        }

        for (CartItem cartItem : new ArrayList<>(cart.getCartItems())) {
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() + cartItem.getQuantity());
            productRepository.save(product);
        }

        cart.getCartItems().clear();
        Cart savedCart = cartRepository.save(cart);
        return cartDtoConverter.convertToDto(savedCart);
    }
}