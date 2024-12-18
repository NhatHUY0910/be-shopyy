package com.demo_shopyy_1.service.impl;

import com.demo_shopyy_1.dto.*;
import com.demo_shopyy_1.exception.ResourceNotFoundException;
import com.demo_shopyy_1.entity.Category;
import com.demo_shopyy_1.entity.Product;
import com.demo_shopyy_1.entity.ProductColor;
import com.demo_shopyy_1.repository.CategoryRepository;
import com.demo_shopyy_1.repository.ProductColorRepository;
import com.demo_shopyy_1.repository.ProductRepository;
import com.demo_shopyy_1.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;
    private final CategoryRepository categoryRepository;
    private final FirebaseStorageService firebaseStorageService;

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
    @Cacheable(value = "productCache", key = "#id")
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public ProductDetailDto getProductDetails(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        return mapToDetailDto(product);
    }

    @Override
    @Transactional
    public Product createProduct(ProductDto productDto) {
        log.info("Starting product creation process for: {}", productDto.getName());

        Product product = new Product();
        updateProductFromDto(product, productDto);
        log.debug("Updated product details from DTO: {}", product);

        processProductImages(product, productDto);
        processProductColors(product, productDto);
        setProductCategory(product, productDto);

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return savedProduct;
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductDto productDto) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    // Chỉ xóa và cập nhật ảnh khi có file mới được tải lên
                    if (productDto.getImageFiles() != null && !productDto.getImageFiles().isEmpty()) {
                        // Giữ lại các URL ảnh cũ nếu chúng vẫn còn trong existingImageUrls
                        List<String> urlsToKeep = productDto.getExistingImageUrls() != null ?
                                productDto.getExistingImageUrls() : new ArrayList<>();

                        // Xóa chỉ những ảnh không còn được sử dụng
                        existingProduct.getImageUrls().stream()
                                .filter(url -> !urlsToKeep.contains(url))
                                .forEach(url -> {
                                    try {
                                        firebaseStorageService.deleteFile(url);
                                    } catch (Exception e) {
                                        log.error("Error deleting file: " + url, e);
                                    }
                                });

                        // Cập nhật danh sách URL
                        existingProduct.getImageUrls().clear();
                        existingProduct.getImageUrls().addAll(urlsToKeep);

                        // Xử lý và tải lên hình ảnh mới
                        processProductImages(existingProduct, productDto);
                    }

                    updateProductFromDto(existingProduct, productDto);
                    updateProductColors(existingProduct, productDto);
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public List<String> getProductNameSuggestions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Giới hạn 5 gợi ý
        Pageable limit = PageRequest.of(0, 5);
        return productRepository.findProductNameSuggestions(keyword.trim(), limit);
    }

    @Override
    public Page<Product> searchProducts(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public Page<Product> getProductsByCategoryPaginated(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    private void processProductImages(Product product, ProductDto productDto) {
        if (productDto.getImageFiles() != null && !productDto.getImageFiles().isEmpty()) {
            try {
                for (MultipartFile imageFile : productDto.getImageFiles()) {
                    if (imageFile != null && !imageFile.isEmpty()) {  // Thêm kiểm tra này
                        log.info("Uploading file: {}", imageFile.getOriginalFilename());
                        String imageUrl = firebaseStorageService.uploadFile(imageFile);
                        log.info("File uploaded successfully, URL: {}", imageUrl);
                        product.getImageUrls().add(imageUrl);
                    }
                }
            } catch (IOException e) {
                log.error("Failed to upload image files for product: {}", productDto.getName(), e);
                throw new RuntimeException("Failed to upload image files", e);
            }
        } else {
            log.info("No image files provided for product: {}", productDto.getName());
        }
    }

    private void processProductColors(Product product, ProductDto productDto) {
        if (productDto.getColors() != null && !productDto.getColors().isEmpty()) {
            for (ProductColorDto colorDto : productDto.getColors()) {
                try {
                    ProductColor color = new ProductColor();
                    color.setName(colorDto.getName());

                    String colorImageUrl = firebaseStorageService.uploadFile(colorDto.getImageFile());
                    color.setImageUrl(colorImageUrl);

                    ProductColor savedColor = productColorRepository.save(color);
                    product.getColors().add(savedColor);
                } catch (IOException e) {
                    log.error("Failed to upload color image", e);
                    throw new RuntimeException("Failed to upload color image", e);
                }
            }
        }
    }

    private void setProductCategory(Product product, ProductDto productDto) {
        if (productDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDto.getCategoryId()));
            product.setCategory(category);
            log.debug("Category set for product: {}", category.getName());
        }
    }

    private void updateProductColors(Product existingProduct, ProductDto productDto) {
        if (productDto.getColors() != null) {
            // Tạo map của các màu hiện có
            Map<Long, ProductColor> existingColors = existingProduct.getColors().stream()
                    .collect(Collectors.toMap(ProductColor::getId, color -> color));

            List<ProductColor> updatedColors = new ArrayList<>();
            for (ProductColorDto colorDto : productDto.getColors()) {
                ProductColor color;
                if (colorDto.getId() != null && existingColors.containsKey(colorDto.getId())) {
                    // Cập nhật màu hiện có
                    color = existingColors.get(colorDto.getId());
                    color.setName(colorDto.getName());

                    // Chỉ cập nhật ảnh nếu có file mới
                    if (colorDto.getImageFile() != null && !colorDto.getImageFile().isEmpty()) {
                        updateColorImage(color, colorDto);
                    }
                } else {
                    // Thêm màu mới
                    color = new ProductColor();
                    color.setName(colorDto.getName());
                    if (colorDto.getImageFile() != null && !colorDto.getImageFile().isEmpty()) {
                        updateColorImage(color, colorDto);
                    } else if (colorDto.getImageUrl() != null && !colorDto.getImageUrl().isEmpty()) {
                        // Giữ lại URL ảnh cũ nếu không có file mới
                        color.setImageUrl(colorDto.getImageUrl());
                    }
                }
                updatedColors.add(productColorRepository.save(color));
            }
            existingProduct.setColors(new HashSet<>(updatedColors));
        }
    }

    private ProductColor processColorUpdate(Product existingProduct, ProductColorDto colorDto) {
        ProductColor color;
        if (colorDto.getId() != null) {
            color = existingProduct.getColors().stream()
                    .filter(c -> c.getId().equals(colorDto.getId()))
                    .findFirst()
                    .orElseGet(ProductColor::new);
        } else {
            color = new ProductColor();
        }

        color.setName(colorDto.getName());
        updateColorImage(color, colorDto);
        return color;
    }

    private void updateColorImage(ProductColor color, ProductColorDto colorDto) {
        if (colorDto.getImageFile() != null) {
            try {
                if (color.getImageUrl() != null) {
                    firebaseStorageService.deleteFile(color.getImageUrl());
                }
                String colorImageUrl = firebaseStorageService.uploadFile(colorDto.getImageFile());
                color.setImageUrl(colorImageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload color image", e);
            }
        }
    }

    private void updateProductFromDto(Product product, ProductDto productDto) {
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setStockQuantity(productDto.getStockQuantity());
        product.setDescription(productDto.getDescription());
        product.setProducer(productDto.getProducer());
        product.setAvailableSizes(productDto.getAvailableSizes());
        product.setAvailableWeights(productDto.getAvailableWeights());

        if (productDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }
    }

    private ProductDetailDto mapToDetailDto(Product product) {
        List<ProductColorDetailDto> colorDtos = product.getColors().stream()
                .map(color -> ProductColorDetailDto.builder()
                        .id(color.getId())
                        .name(color.getName())
                        .imageUrl(color.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return ProductDetailDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrls(product.getImageUrls())
                .colors(colorDtos)
                .availableSizes(product.getAvailableSizes())
                .availableWeights(product.getAvailableWeights())
                .producer(product.getProducer())
                .category(product.getCategory() != null ? new CategoryDto(
                        product.getCategory().getId(),
                        product.getCategory().getName()
                ) : null)
                .build();
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
