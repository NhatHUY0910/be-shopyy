package com.demo_shopyy_1.service.impl;

import com.demo_shopyy_1.entity.Category;
import com.demo_shopyy_1.dto.CategoryDto;
import com.demo_shopyy_1.repository.CategoryRepository;
import com.demo_shopyy_1.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category createCategory(CategoryDto categoryDto) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long id, CategoryDto categoryDto) {
        return null;
    }

    @Override
    public void deleteCategory(Long id) {
    }

    @Override
    public Page<Category> getCategoriesPaginated(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
}
