package com.demo_shopyy_1.service.impl;

import com.demo_shopyy_1.model.Order;
import com.demo_shopyy_1.model.OrderItem;
import com.demo_shopyy_1.model.Product;
import com.demo_shopyy_1.model.User;
import com.demo_shopyy_1.model.dto.CreateOrderRequestDto;
import com.demo_shopyy_1.model.dto.OrderItemDto;
import com.demo_shopyy_1.model.dto.OrderResponseDto;
import com.demo_shopyy_1.repository.OrderRepository;
import com.demo_shopyy_1.repository.ProductRepository;
import com.demo_shopyy_1.repository.UserRepository;
import com.demo_shopyy_1.service.CartService;
import com.demo_shopyy_1.service.UserService;
import com.demo_shopyy_1.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private CartService cartService;

    @Override
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto requestDto, HttpServletRequest request) throws UnsupportedEncodingException {
        log.info("Creating order with data: {}", requestDto);
        User currentUser = userService.getCurrentUser();
        Order order = createOrderFromRequest(currentUser, requestDto);
        log.info("Order created: {}", order);

        // Kiểm tra số lượng tồn kho trước khi tạo đơn hàng
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Không đủ số lượng cho sản phẩm: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        orderRepository.save(order);

        for(OrderItem item : order.getOrderItems()) {
            cartService.removeCartItem(currentUser, item.getProduct().getId());
        }

        if (requestDto.getPaymentMethod() == Order.PaymentMethod.VNPAY) {
            String paymentUrl = vnPayService.createPaymentUrl(request, order);
            order.setVnpayPaymentUrl(paymentUrl);
            orderRepository.save(order);

            OrderResponseDto responseDto = convertToOrderResponseDto(order);
            responseDto.setPaymentUrl(paymentUrl);
            return responseDto;
        }

        return convertToOrderResponseDto(order);
    }

    private Order createOrderFromRequest(User user, CreateOrderRequestDto requestDto) {
//        User currentUser = userService.getCurrentUser();
//        log.info("Current user: {}", currentUser);

        Order order = new Order();
//        order.setUser(currentUser);
        order.setUser(user);
        order.setOrderCode(generateOrderCode());
        order.setFullName(requestDto.getFullName());
        order.setPhoneNumber(requestDto.getPhoneNumber());
        order.setAddress(requestDto.getAddress());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setPaymentMethod(requestDto.getPaymentMethod());
        order.setOrderDate(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemDto itemDto : requestDto.getOrderItems()) {
            OrderItem orderItem = createOrderItem(itemDto.getProductId(), itemDto.getQuantity(), order);
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setShippingFee(new BigDecimal("30000"));

        return order;
    }

    private OrderItem createOrderItem(Long productId, Integer quantity, Order order) {
        if (productId == null) {
            throw new IllegalArgumentException("ProductId cannot be null");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setPrice(product.getPrice());
        orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));

        return orderItem;
    }

    @Override
    public String generateOrderCode() {
        return UUID.randomUUID().toString(); // Sử dụng UUID để đảm bảo tính duy nhất
    }

    @Override
    public OrderResponseDto convertToOrderResponseDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setFullName(order.getFullName());
        dto.setPhoneNumber(order.getPhoneNumber());
        dto.setAddress(order.getAddress());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentTransactionId(order.getPaymentTransactionId());
        dto.setCreatedAt(order.getOrderDate());
        dto.setUpdatedAt(order.getUpdateAt());

        // Convert OrderItems to OrderItemDtos
        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(this::convertToOrderItemDto)
                .collect(Collectors.toList());
        dto.setOrderItems(orderItemDtos);

        return dto;
    }

    private OrderItemDto convertToOrderItemDto(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setSubtotal(orderItem.getSubtotal());
        dto.setImageUrl(orderItem.getProduct().getImageUrl()); // Add this line
        return dto;
    }
}
