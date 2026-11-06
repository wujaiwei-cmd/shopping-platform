
package com.example.shopping.service;

import com.example.shopping.entity.Category;
import com.example.shopping.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findByEnabledTrueOrderBySortOrder();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("分类名称已存在");
        }

        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getEnabled() == null) {
            category.setEnabled(true);
        }

        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category category) {
        Category existing = getCategoryById(id);

        if (category.getName() != null && !category.getName().equals(existing.getName())) {
            if (categoryRepository.existsByName(category.getName())) {
                throw new RuntimeException("分类名称已存在");
            }
            existing.setName(category.getName());
        }

        if (category.getDescription() != null) {
            existing.setDescription(category.getDescription());
        }

        if (category.getSortOrder() != null) {
            existing.setSortOrder(category.getSortOrder());
        }

        if (category.getEnabled() != null) {
            existing.setEnabled(category.getEnabled());
        }

        return categoryRepository.save(existing);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("分类不存在");
        }
        categoryRepository.deleteById(id);
    }

    public Category toggleCategoryStatus(Long id) {
        Category category = getCategoryById(id);
        category.setEnabled(!category.getEnabled());
        return categoryRepository.save(category);
    }
}
