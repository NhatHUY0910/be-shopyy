package com.demo_shopyy_1.controller;

import com.demo_shopyy_1.exception.ResourceNotFoundException;
import com.demo_shopyy_1.model.Product;
import com.demo_shopyy_1.model.dto.PagedResponseDto;
import com.demo_shopyy_1.model.dto.ProductDetailDto;
import com.demo_shopyy_1.model.dto.ProductDto;
import com.demo_shopyy_1.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
//@CrossOrigin("*")
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> findAll() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/paginated")
    public ResponseEntity<PagedResponseDto<Product>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Product> productPage = productService.getProductsPaginated(pageRequest);

        PagedResponseDto<Product> response = new PagedResponseDto<>();
        response.setContent(productPage.getContent());
        response.setPageNumber(productPage.getNumber());
        response.setPageSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());
        response.setLast(productPage.isLast());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<ProductDetailDto> getProductDetails(@PathVariable Long id) {
        try {
            ProductDetailDto productDto = productService.getProductDetails(id);
            return ResponseEntity.ok(productDto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @ModelAttribute ProductDto productDto) {

        if (imageFiles != null) {
            if (imageFiles.size() > 5) {  // Giới hạn số lượng file
                return ResponseEntity.badRequest().body("Maximum 5 images allowed");
            }

            for (MultipartFile file : imageFiles) {
                if (file.getSize() > 5_000_000) {  // Giới hạn kích thước file (5MB)
                    return ResponseEntity.badRequest().body("File size should not exceed 5MB");
                }

                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest().body("Only image files are allowed");
                }
            }
        }

        try {
            productDto.setImageFiles(imageFiles);
            log.info("Received product DTO: {}", productDto);

            if (!imageFiles.isEmpty()) {
                log.info("Received {} files", imageFiles.size());
                for (MultipartFile file : imageFiles) {
                    log.info("File: {}, size: {}", file.getOriginalFilename(), file.getSize());
                }
            } else {
                log.warn("No files received");
            }

            Product newProduct = productService.createProduct(productDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating product: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @ModelAttribute ProductDto productDto) {
        Product updatedProduct = productService.updateProduct(id, productDto);
        if (updatedProduct != null) {
            return ResponseEntity.ok(updatedProduct);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
