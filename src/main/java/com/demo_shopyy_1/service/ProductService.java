package com.demo_shopyy_1.service;

import com.demo_shopyy_1.model.Product;
import com.demo_shopyy_1.model.dto.ProductDto;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> getAllProducts();
    Optional<Product> getProductById(Long id);
    Product createProduct(ProductDto productDto);
    Product updateProduct(Long id, ProductDto productDto);
    void deleteProduct(Long id);
}
