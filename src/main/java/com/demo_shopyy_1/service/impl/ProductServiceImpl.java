package com.demo_shopyy_1.service.impl;

import com.demo_shopyy_1.model.Category;
import com.demo_shopyy_1.model.Product;
import com.demo_shopyy_1.model.dto.ProductDto;
import com.demo_shopyy_1.repository.CategoryRepository;
import com.demo_shopyy_1.repository.ProductRepository;
import com.demo_shopyy_1.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product createProduct(ProductDto productDto) {
        Product product = new Product();
        updateProductFromDto(product, productDto);

        if (productDto.getImageFile() != null && !productDto.getImageFile().isEmpty()) {
            String fileName = saveImage(productDto.getImageFile());
            product.setImageUrl(fileName);
        }
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, ProductDto productDto) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            updateProductFromDto(product, productDto);

            if (productDto.getImageFile() != null && !productDto.getImageFile().isEmpty()) {
                String fileName = saveImage(productDto.getImageFile());
                product.setImageUrl(fileName);
            }
            return productRepository.save(product);
        }
        return null;
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    private void updateProductFromDto(Product product, ProductDto productDto) {
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setStockQuantity(productDto.getStockQuantity());
        product.setDescription(productDto.getDescription());
        product.setProducer(productDto.getProducer());

        if (productDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

    }

    private String saveImage(MultipartFile imageFile) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            Path filePath = Paths.get(uploadPath, fileName);
            Files.write(filePath, imageFile.getBytes());
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("failed to save image file ", e);
        }
    }
}
