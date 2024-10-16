package com.demo_shopyy_1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "products")
//@JsonIgnoreProperties({"category"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int stockQuantity;

    private String description;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", length = 1000)
    private List<String> imageUrls = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "product_color_mapping",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "color_id")
    )
    private Set<ProductColor> colors = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "product_sizes", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "size")
    private Set<String> availableSizes = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "product_weights", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "weight")
    private Set<String> availableWeights = new HashSet<>();

    private String producer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "products"})
//    @JsonIgnore
    private Category category;

    public String getDefaultImageUrl() {
        // lấy hình ảnh mới nhất
//        return imageUrls != null && !imageUrls.isEmpty() ? imageUrls.get(imageUrls.size() - 1) : null;

        //lấy hình ảnh đầu tiên
        return imageUrls != null && !imageUrls.isEmpty() ? imageUrls.get(0) : null;
    }
}
