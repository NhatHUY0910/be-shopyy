package com.demo_shopyy_1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductColorDetailDto {
    private Long id;
    private String name;
    private String imageUrl;
}
