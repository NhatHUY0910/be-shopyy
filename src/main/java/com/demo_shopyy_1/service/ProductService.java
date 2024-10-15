package com.demo_shopyy_1.service;

import com.demo_shopyy_1.model.Product;
import com.demo_shopyy_1.model.dto.ProductDetailDto;
import com.demo_shopyy_1.model.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> getAllProducts();
    Page<Product> getProductsPaginated(Pageable pageable);
    Optional<Product> getProductById(Long id);
    ProductDetailDto getProductDetails(Long id);
    Product createProduct(ProductDto productDto);
    Product updateProduct(Long id, ProductDto productDto);
    void deleteProduct(Long id);
}
