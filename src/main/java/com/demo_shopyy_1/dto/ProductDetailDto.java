package com.demo_shopyy_1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private List<String> imageUrls;
    private List<ProductColorDetailDto> colors;
    private Set<String> availableSizes;
    private Set<String> availableWeights;
    private String producer;
    private CategoryDto category;
}
