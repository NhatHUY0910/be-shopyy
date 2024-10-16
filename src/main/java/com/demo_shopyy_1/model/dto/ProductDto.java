package com.demo_shopyy_1.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;

//    @NotBlank(message = "Product name is required")
//    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private int stockQuantity;

//    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String producer;

//    @Size(max = 5, message = "Maximum 5 image files allowed")
    private List<MultipartFile> imageFiles;

//    @NotNull(message = "Category ID is required")
    private Long categoryId;

//    @Valid
    private List<ProductColorDto> colors;

    private Set<String> availableSizes;

//    @Pattern(regexp = "^\\d+(\\.\\d{1,2})?\\s*(g|kg|oz|lb)$", message = "Invalid weight format")
    private Set<String> availableWeights;
}
