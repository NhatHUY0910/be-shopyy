package com.demo_shopyy_1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToCartDto {

    @JsonProperty("productId")
    private Long productId;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("colorId")
    private Long colorId;

    @JsonProperty("size")
    private String size;

    @JsonProperty("weight")
    private String weight;
}
