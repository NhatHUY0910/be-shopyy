package com.demo_shopyy_1.repository;

import com.demo_shopyy_1.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByOrderCode(String orderCode);
}