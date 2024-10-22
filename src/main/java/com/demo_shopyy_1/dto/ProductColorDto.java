package com.demo_shopyy_1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductColorDto {
    private Long id;

//    @NotBlank(message = "Color name is required")
//    @Size(min = 2, max = 50, message = "Color name must be between 2 and 50 characters")
    private String name;

    private MultipartFile imageFile;
}
