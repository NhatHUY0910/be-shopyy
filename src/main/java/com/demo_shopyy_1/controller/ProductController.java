package com.demo_shopyy_1.controller;

import com.demo_shopyy_1.model.Product;
import com.demo_shopyy_1.model.dto.ProductDto;
import com.demo_shopyy_1.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestPart("imageFile") MultipartFile imageFile, @ModelAttribute ProductDto productDto) {
        try {
            productDto.setImageFile(imageFile);
            log.info("Received product DTO: {}", productDto);

            if (imageFile != null && !imageFile.isEmpty()) {
                log.info("Received file: {}, size: {}", imageFile.getOriginalFilename(), imageFile.getSize());
            } else {
                log.warn("No file received");
            }

            Product newProduct = productService.createProduct(productDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating product: " + e.getMessage());
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
