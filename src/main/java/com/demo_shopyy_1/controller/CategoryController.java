package com.demo_shopyy_1.controller;

import com.demo_shopyy_1.dto.PagedResponseDto;
import com.demo_shopyy_1.entity.Category;
import com.demo_shopyy_1.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/paginated")
    public ResponseEntity<PagedResponseDto<Category>> getCategoriesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        try {
            if (page < 0) page = 0;
            if (size <= 0) size = 5;

            PageRequest pageRequest = PageRequest.of(page, size);
            Page<Category> categoryPage = categoryService.getCategoriesPaginated(pageRequest);

            PagedResponseDto<Category> response = new PagedResponseDto<>();
            response.setContent(categoryPage.getContent());
            response.setPageNumber(categoryPage.getNumber());
            response.setPageSize(categoryPage.getSize());
            response.setTotalElements(categoryPage.getTotalElements());
            response.setTotalPages(categoryPage.getTotalPages());
            response.setLast(categoryPage.isLast());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
