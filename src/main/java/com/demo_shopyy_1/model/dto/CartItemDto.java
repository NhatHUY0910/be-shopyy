package com.demo_shopyy_1.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private String imageUrl;
    private int stockQuantity;
    private String selectedColorName;
    private String selectedSize;
    private String selectedWeight;

    // Thêm một phương thức để lấy chuỗi phân loại hàng
    public String getVariantInfo() {
        StringBuilder variant = new StringBuilder();
        if (selectedColorName != null) {
            variant.append("Màu: ").append(selectedColorName);
        }
        if (selectedSize != null) {
            if (!variant.isEmpty()) variant.append(", ");
            variant.append("Size: ").append(selectedSize);
        }
        if (selectedWeight != null) {
            if (!variant.isEmpty()) variant.append(", ");
            variant.append("Khối lượng: ").append(selectedWeight);
        }

        return !variant.isEmpty() ? variant.toString() : "Không có phân loại";
    }
}