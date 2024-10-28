package com.demo_shopyy_1.service;

import com.demo_shopyy_1.entity.Category;
import com.demo_shopyy_1.dto.CategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(Long id);
    Category createCategory(CategoryDto categoryDto);
    Category updateCategory(Long id, CategoryDto categoryDto);
    void deleteCategory(Long id);
    Page<Category> getCategoriesPaginated(Pageable pageable);
}
