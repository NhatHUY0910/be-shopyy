package com.demo_shopyy_1.service.impl;

import com.demo_shopyy_1.model.Category;
import com.demo_shopyy_1.model.Product;
import com.demo_shopyy_1.model.dto.ProductDto;
import com.demo_shopyy_1.repository.CategoryRepository;
import com.demo_shopyy_1.repository.ProductRepository;
import com.demo_shopyy_1.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FirebaseStorageService firebaseStorageService;

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> getProductsPaginated(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product createProduct(ProductDto productDto) {
        log.info("Starting product creation process for: {}", productDto.getName());
        log.info("Image file: {}", productDto.getImageFile() != null ? productDto.getImageFile().getOriginalFilename() : "No file");

        Product product = new Product();
        updateProductFromDto(product, productDto);
        log.debug("Updated product details from DTO: {}", product);

        if (productDto.getImageFile() != null && !productDto.getImageFile().isEmpty()) {
            try {
                log.info("Uploading file: {}", productDto.getImageFile().getOriginalFilename());
                String imageUrl = firebaseStorageService.uploadFile(productDto.getImageFile());
                log.info("File uploaded successfully, URL: {}", imageUrl);
                product.setImageUrl(imageUrl);
            } catch (IOException e) {
                log.error("Failed to upload image file for product: {}", productDto.getName(), e);
                throw new RuntimeException("Failed to upload image file", e);
            }
        } else {
            log.info("No image file provided for product: {}", productDto.getName());
        }

        if (productDto.getCategoryId() != null) {
            try {
                Category category = categoryRepository.findById(productDto.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                product.setCategory(category);
                log.debug("Category set for product: {}", category.getName());
            } catch (RuntimeException e) {
                log.error("Category not found for ID: {}", productDto.getCategoryId(), e);
                throw e;
            }
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return savedProduct;
    }

    @Override
    public Product updateProduct(Long id, ProductDto productDto) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            updateProductFromDto(product, productDto);

            if (productDto.getImageFile() != null && !productDto.getImageFile().isEmpty()) {
                try {
                    String newImageUrl = firebaseStorageService.uploadFile(productDto.getImageFile());
                    // If new image upload is successful, delete the old image
                    if (product.getImageUrl() != null) {
                        String oldFileName = extractFileNameFromUrl(product.getImageUrl());
                        firebaseStorageService.deleteFile(oldFileName);
                    }
                    product.setImageUrl(newImageUrl);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload image file", e);
                }
            }
            return productRepository.save(product);
        }
        return null;
    }

    private String extractFileNameFromUrl(String url) {
        // Implement this method to extract the file name from the Firebase URL
        // This might depend on how your Firebase URLs are structured
        return url.substring(url.lastIndexOf('/') + 1);
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
