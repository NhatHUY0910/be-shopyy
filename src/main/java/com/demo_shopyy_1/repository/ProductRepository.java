package com.demo_shopyy_1.repository;

import com.demo_shopyy_1.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Tìm kiếm sản phẩm theo tên chứa keyword (không phân biệt hoa thường)
    @Query("select p from Product p where lower(p.name) like lower(concat('%', :keyword, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("keyword") String keyword);

    // Tìm kiếm sản phẩm theo tên chứa keyword với phân trang
    @Query("select p from Product p where lower(p.name) like lower(concat('%', :keyword, '%'))")
    Page<Product> findByNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    // Tìm kiếm tên sản phẩm để gợi ý (giới hạn số lượng kết quả)
    @Query("select p.name from Product p where lower(p.name) like lower(concat('%', :keyword, '%'))")
    List<String> findProductNameSuggestions(@Param("keyword") String keyword, Pageable pageable);

    @Query("select p from Product p where p.category.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("select p from Product p where p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    @NonNull
    Page<Product> findAll(@NonNull Pageable pageable);
}
