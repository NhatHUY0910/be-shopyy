package com.demo_shopyy_1.service.impl;

import com.demo_shopyy_1.model.*;
import com.demo_shopyy_1.model.dto.CartDtoConverter;
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
import java.util.Objects;

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

        // Validate quantity
        if (addToCartDto.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        if (addToCartDto.getQuantity() > product.getStockQuantity()) {
            throw new RuntimeException("Quantity must be less than or equal to product's quantity");
        }

        // Validate color selection if product has colors
        ProductColor selectedColor = null;
        if (!product.getColors().isEmpty()) {
            if (addToCartDto.getColorId() == null) {
                throw new RuntimeException("Please select a color for this product");
            }
            selectedColor = product.getColors().stream()
                    .filter(color -> color.getId().equals(addToCartDto.getColorId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Selected color not found"));
        }

        // Validate size selection if product has sizes
        if (!product.getAvailableSizes().isEmpty() && addToCartDto.getSize() == null) {
            throw new RuntimeException("Please select a size for this product");
        }

        // Validate weight selection if product has weights
        if (!product.getAvailableWeights().isEmpty() && addToCartDto.getWeight() == null) {
            throw new RuntimeException("Please select a weight for this product");
        }

        // Initialize cart if not exists
        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
            user.setCart(cart);
        }

        // Check if same product with same variants exists in cart
        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId())
                        && Objects.equals(item.getSelectedColor() != null ? item.getSelectedColor().getId() : null,
                        addToCartDto.getColorId())
                        && Objects.equals(item.getSelectedSize(), addToCartDto.getSize())
                        && Objects.equals(item.getSelectedWeight(), addToCartDto.getWeight()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update quantity of existing item
            int newQuantity = existingItem.getQuantity() + addToCartDto.getQuantity();
            if (newQuantity > product.getStockQuantity()) {
                throw new RuntimeException("Total quantity would exceed available stock");
            }
            existingItem.setQuantity(newQuantity);
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(addToCartDto.getQuantity());
            newItem.setSelectedColor(selectedColor);
            newItem.setSelectedSize(addToCartDto.getSize());
            newItem.setSelectedWeight(addToCartDto.getWeight());
            cart.addCartItem(newItem);
        }

        // Update product stock
        product.setStockQuantity(product.getStockQuantity() - addToCartDto.getQuantity());
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
