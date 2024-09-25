package com.demo_shopyy_1.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponseDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private int stockQuantity;
    private String description;
    private String producer;
    private String imageUrl;
    private CategoryDto category;
}
