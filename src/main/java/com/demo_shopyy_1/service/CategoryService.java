package com.demo_shopyy_1.service;

import com.demo_shopyy_1.model.Category;
import com.demo_shopyy_1.model.dto.CategoryDto;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(Long id);
    Category createCategory(CategoryDto categoryDto);
    Category updateCategory(Long id, CategoryDto categoryDto);
    void deleteCategory(Long id);
}
